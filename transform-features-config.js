const fs = require('fs');
const child = require('child_process');

let data = JSON.parse(fs.readFileSync(process.argv[2] || 'features.json').toString('utf8'));
let lines = fs.readFileSync(process.argv[3] || 'src/main/resources/default_features_config.ini.inc').toString('utf8').split(/\r?\n/g);
let out = "";
let lineNum = 0;
let featureReplacementRegex = /^; << FEATURE: (.*) >>$/;
lines.forEach((line) => {
	lineNum++;
	let trim = line.trim();
	let leadingTabs = 0;
	for (let i = 0; i < line.length; i++) {
		if (line.charAt(i) == '\t') {
			leadingTabs++;
		} else {
			break;
		}
	}
	let res = featureReplacementRegex.exec(trim);
	if (res) {
		let datum = data[res[1]];
		let desc;
		if (datum) {
			desc = datum.desc;
		} else {
			console.error("At line "+lineNum+": Unknown key "+res[1]);
			desc = "ERROR: Unknown key "+res[1];
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
	} else {
		out += line;
		out += '\r\n';
	}
});
fs.writeFileSync(process.argv[4] || 'src/main/resources/default_features_config.ini', out);
