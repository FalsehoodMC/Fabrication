const fs = require('fs');

let versionNamesToCodes = {
	'1.0.0': 0,
	'1.0': 0,
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
	'1.2.8': 22,
	'1.2.9': 23,
	'1.2.10': 24,
	'1.2.11': 25,
	'1.3.0': 26,
	'1.3.1': 27,
	'1.3.2': 28,
	'1.3.2_01': 28,
	'1.3.3': 29,
	'1.3.4': 30,
	'1.3.4_01': 30,
	'1.3.4_02': 30,
	'1.3.5-pre1': 31,
	'1.3.5': 31,
	'2.0.0': 40,
	'2.1.0': 41
};

let currentVersion = /version\s+=\s+(.*?)\s+/.exec(fs.readFileSync('gradle.properties').toString('utf8'))[1];
let currentVersionCode = versionNamesToCodes[currentVersion];

let data = [];
let lines = fs.readFileSync(process.argv[2] || 'features.txt').toString('utf8').split(/\r?\n/g);
let curKey = null;
let cur = {};
let defaults = (curKey, cur) => ({
	name: curKey,
	short_name: cur && cur.name || null,
	meta: false,
	section: false,
	hidden: false,
	since: null,
	since_code: cur && cur.since ? versionNamesToCodes[cur.since] : null,
	sides: "irrelevant",
	needs: [],
	default: "inherit",
	parent: null,
	media: null,
	media_text: cur && cur.media ? (/\.mp4$/.exec(cur.media) ? 'Demonstration video' : 'Demonstration image') : null,
	extra_media: null,
	extra_media_text: cur && cur.extra_media ? (/\.mp4$/.exec(cur.extra_media) ? 'Extra demonstration video' : 'Extra demonstration image') : null,
	link_url: null,
	link_text: cur && cur.link_url ? 'See also' : null,
	short_desc: cur && cur.desc ? cur.desc.search(/\.( |\n|$)/) !== -1 ? cur.desc.substring(0, cur.desc.search(/\.( |\n|$)/)) : cur.desc : null,
	desc: null,
	brand_new: cur && cur.since && cur.since === currentVersion,
	fscript: null,
	fscript_default: null,
	new: (cur ? cur.since_code ? cur.since_code : versionNamesToCodes[cur.since] : 9999) >= currentVersionCode-1
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
	cur.key = curKey;
	data.push(cur);
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
					case 'needs':
						v = v.split(' ');
						break;
					case 'endorsed': case 'hidden': case 'section': case 'meta':
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
let sections = ["general", "fixes", "utility", "tweaks", "minor_mechanics", "mechanics", "balance", "weird_tweaks", "woina", "pedantry", "situational", "experiments"];
data.sort((a, b) => {
	let sectionA = a.key.indexOf('.') === -1 ? a.key : a.key.substring(0, a.key.indexOf('.'));
	let sectionB = b.key.indexOf('.') === -1 ? b.key : b.key.substring(0, b.key.indexOf('.'));
	if (sectionA === sectionB && sectionA === a.key) return -1;
	if (sectionA === sectionB && sectionB === b.key) return 1;
	if (sectionA !== sectionB) return sections.indexOf(sectionA) - sections.indexOf(sectionB);
	if (a.meta !== b.meta) return a.meta ? -1 : 1;
	return a.key.localeCompare(b.key);
});

let dataObj = {};
data.forEach((d) => {
	let key = d.key;
	delete d.key;
	dataObj[key] = d;
});

if (process.argv[3]) {
	fs.writeFileSync(process.argv[3], JSON.stringify(dataObj, (k, v) => { if (v !== null) return v; }));
} else {
	console.log(JSON.stringify(dataObj), (k, v) => { if (v !== null) return v; });
}
