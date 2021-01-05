const fs = require('fs');

let versionNamesToCodes = {
	'1.0.0': 0,
	'1.0.1': 1,
	'1.0.1_01': 2,
	'1.0.2': 3,
	'1.1': 4,
	'1.1_01': 5,
	'1.1_02': 6,
	'1.1.1': 7,
	'1.1.1_01': 8,
	'1.1.2': 9,
	'1.2': 10,
	'1.2_01': 11,
	'1.2.1': 12,
	'1.2.2': 13,
	'1.2.2_01': 14,
	'1.2.3': 15,
	'1.2.3_01': 16,
	'1.2.3_02': 17,
	'1.2.4': 18,
	'1.2.5': 19,
	'1.2.6': 20,
	'1.2.7': 21,
	'1.2.8': 28
};

let data = {};
let lines = fs.readFileSync(process.argv[2] || 'features.txt').toString('utf8').split(/\r?\n/g);
let curKey = null;
let cur = {};
let defaults = (curKey, cur) => ({
	name: curKey,
	short_name: cur && cur.name || null,
	meta: false,
	since: null,
	since_code: cur && cur.since ? versionNamesToCodes[cur.since] : null,
	sides: [],
	needs: [],
	endorsed: true,
	parent: null,
	media: null,
	media_text: cur && cur.media ? (/\.mp4$/.exec(cur.media) ? 'Demonstration video' : 'Demonstration image') : null,
	extra_media: null,
	extra_media_text: cur && cur.extra_media ? (/\.mp4$/.exec(cur.extra_media) ? 'Extra demonstration video' : 'Extra demonstration image') : null,
	link_url: null,
	link_text: cur && cur.link_url ? 'See also' : null,
	desc: null
});
let lineNum = 0;
let multilineKey = null
let multilineBuf = "";

function commitMultiline() {
	if (multilineKey !== null) {
		cur[multilineKey] = multilineBuf.trim().replace(/ +\n/g, '\n');
	}
	multilineKey = null;
	multilineBuf = "";
}

function commit() {
	commitMultiline();
	cur = Object.assign(defaults(curKey, cur), cur);
	data[curKey] = cur;
	curKey = null;
	cur = {};
}

lines.forEach((line) => {
	lineNum++;
	let trim = line.trim();
	if (trim.indexOf('#') == 0) return;
	let leadingTabs = 0;
	for (let i = 0; i < line.length; i++) {
		if (line.charAt(i) == '\t') {
			leadingTabs++;
		} else {
			break;
		}
	}
	if (leadingTabs == 0) {
		if (trim.length > 0) {
			if (curKey != null) {
				commit();
			}
			curKey = trim;
		} else {
			if (multilineKey !== null) {
				multilineBuf += "\n";
			}
		}
	} else if (curKey == null) {
		console.error("At line "+lineNum+": Got an indented line before a key definition. Ignoring");
		return;
	} else {
		if (leadingTabs == 1) {
			if (multilineKey !== null) {
				commitMultiline();
			}
			let colonIdx = trim.indexOf(':');
			if (colonIdx == -1) {
				console.error("At line "+lineNum+": Got a single-indented line with no colon. Ignoring");
				return;
			}
			let k = trim.substring(0, colonIdx).trim();
			if (typeof defaults(curKey, null)[k] === 'undefined') {
				console.error("At line "+lineNum+": Got an unknown key "+k+". Ignoring");
				return;
			}
			let v = trim.substring(colonIdx+1).trim();
			if (v === '') {
				multilineKey = k;
			} else {
				switch (k) {
					case 'sides': case 'needs':
						v = v.split(' ');
						break;
					case 'endorsed':
						v = (v === 'false' ? false : v === 'true' ? true : v);
						break;
				}
				cur[k] = v;
			}
		} else if (leadingTabs >= 2) {
			if (multilineKey !== null) {
				if (trim.length === 0) {
					// paragraph separator
					multilineBuf += "\n\n";
				} else {
					multilineBuf += line.substring(2)+" ";
					if (/  $/.exec(line)) multilineBuf += "\n";
				}
			}
		}
	}
});
if (curKey !== null) commit();

if (process.argv[3]) {
	fs.writeFileSync(process.argv[3], JSON.stringify(data));
} else {
	console.log(JSON.stringify(data));
}
