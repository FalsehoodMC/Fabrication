const fs = require('fs');
const child = require('child_process');

let data = JSON.parse(fs.readFileSync(process.argv[2] || 'features.json').toString('utf8'));
let lines = fs.readFileSync(process.argv[3] || 'src/main/resources/default_features_config.ini.tmpl').toString('utf8').split(/\r?\n/g);
let out = "";
let lineNum = 0;
lines.forEach((line) => {
	lineNum++;
	let trim = line.trim();
	if (trim === ';!!;') {
		Object.entries(data).forEach(([k, datum]) => {
			let leadingTabs = 0;
			if (k.indexOf('.') !== -1) {
				leadingTabs = 1;
				if (datum.parent) {
					leadingTabs++;
					if (datum.meta) {
						return;
					}
				}
			} else if (datum.meta) {
				return;
			}
			let val = "unset";
			let desc = datum.desc;
			if (k === 'general.profile') {
				desc += '\n\nThe available profiles are:\n';
				['green', 'blonde', 'light', 'medium', 'dark', 'vienna', 'burnt'].forEach((p) => {
					en = data['general.profile.'+p];
					desc += '* '+en.name+': '+en.desc+'\n';
				});
				val = "light";
			} else if (k === "general.runtime_checks") {
				val = "true";
			} else if (k === "general.reduced_motion") {
				val = "false";
			} else if (k === "general.data_upload") {
				val = "false";
			}
			desc = child.spawnSync('fold', ['-s', '-w', 78-(leadingTabs*8)], {input: desc}).stdout.toString('utf8');
			desc = desc.replace(/\r?\n\*(.*?)\r?\n([^*].*?)/g, "\n*$1\n  $2");
			if (datum && (datum.media || datum.extra_media || datum.link_url)) desc += '\n';
			if (datum && datum.media) {
				desc += '\n'+datum.media_text+': '+datum.media;
			}
			if (datum && datum.extra_media) {
				desc += '\n'+datum.extra_media_text+': '+datum.extra_media;
			}
			if (datum && datum.link_url) {
				desc += '\n'+datum.link_text+': '+datum.link_url;
			}
			let tabs = '\t'.repeat(leadingTabs);
			desc = tabs+'; '+desc;
			out += desc.replace(/\r?\n/g, '\r\n'+tabs+'; ')+'\r\n';
			if (k.indexOf('.') !== -1) {
				out += tabs+k.substring(k.indexOf('.')+1)+"="+val+"\r\n\r\n";
			} else {
				out += tabs+"["+k+"]\r\n";
			}
		});
	} else {
		out += line;
		out += '\r\n';
	}
});
fs.writeFileSync(process.argv[4] || 'src/main/resources/default_features_config.ini', out);
