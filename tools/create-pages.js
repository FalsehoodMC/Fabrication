const fs = require('fs');
const path = require('path');
const hbs = require('handlebars');
const md = require('markdown-it')();

let data = JSON.parse(fs.readFileSync(process.argv[2] || 'src/main/resources/features.json').toString('utf8'));
let metaSection = {name: 'Meta', key: 'meta', features: [], features_incl_meta: []};
let ctx = {feature_count:0,sections:[],sections_incl_meta:[metaSection]};
let buildingSection = null;
let pastFeatureCounts = {};
let featureCount = 0;
Object.entries(data).forEach(([k, v]) => {
	if (k.indexOf('general.profile.') === 0) return;
	if (k.indexOf('.') !== -1 || v.meta) {
		let section = k.indexOf('.') === -1 ? metaSection : buildingSection;
		for (let i = v.since_code; i < 100; i++) {
			pastFeatureCounts[i] = (pastFeatureCounts[i] || 0) + 1;
		}
		featureCount++;
		if (!v.hidden) {
			let sides_friendly = null;
			switch (v.sides) {
				case "irrelevant": break;
				case "either": sides_friendly = "Server or Client"; break;
				case "client_only": sides_friendly = "Client Only"; break;
				case "server_only": sides_friendly = "Server Only"; break;
				case "server_only_with_client_helper": sides_friendly = "Server & Client (Client Optional)"; break;
				case "server_and_client": sides_friendly = "Server & Client"; break;
			}
			let desc = v.desc;
			if (k === 'general.profile') {
				desc += '\n\nThe available profiles are:\n';
				['green', 'blonde', 'light', 'medium', 'dark', 'vienna', 'burnt'].forEach((p) => {
					en = data['general.profile.'+p];
					desc += ' * **'+en.name+'**: '+en.desc+'\n';
				});
			}
			let obj = {
				...v,
				key: k,
				keyless: v.meta && k.indexOf('.') === -1,
				desc_html: md.render(desc),
				media_video: v.media && /\.mp4$/.exec(v.media),
				media_poster: v.media && v.media.replace('.mp4', '-poster.jpg'),
				extra_media_video: v.extra_media && /\.mp4$/.exec(v.extra_media),
				extra_media_poster: v.extra_media && v.extra_media.replace('.mp4', '-poster.jpg'),
				sides_friendly
			};
			section.features_incl_meta.push(obj);
			if (!v.meta) {
				section.features.push(obj);
			}
		}
	} else if (!v.hidden) {
		if (v.meta) return;
		if (buildingSection !== null) {
			if (buildingSection.features.length > 0) ctx.sections.push(buildingSection);
			ctx.sections_incl_meta.push(buildingSection);
		}
		buildingSection = {...v, key: k, desc_html: md.render(v.desc), features:[], features_incl_meta:[]};
	}
});
if (buildingSection !== null) {
	if (buildingSection.features.length > 0) ctx.sections.push(buildingSection);
	ctx.sections_incl_meta.push(buildingSection);
}
ctx.feature_count = featureCount;
let pfc = Object.values(pastFeatureCounts).sort((a, b) => a-b);
pfc = pfc.filter((e, i) => pfc.indexOf(e) === i);
if (pfc.length > 15) pfc = pfc.slice(-15);
ctx.past_feature_counts = pfc.map((e, i) => { let s = Math.max(0.45, Math.min(1, (i+1)/12)); return {
	count: e, size: s, sizePx: Math.max(6, s*12)};});
let me = path.resolve(process.argv[1], '..');
let render = hbs.compile(fs.readFileSync(path.resolve(me, 'curse.html.hbs')).toString('utf8'));
fs.writeFileSync('curse-fabrication.html', render({...ctx, fabrication: true}));
fs.writeFileSync('curse-forgery.html', render({...ctx, fabrication: false}));
fs.writeFileSync('modrinth.md', hbs.compile(fs.readFileSync(path.resolve(me, 'modrinth.md.hbs')).toString('utf8'))(ctx));
fs.writeFileSync('wiki.html', hbs.compile(fs.readFileSync(path.resolve(me, 'wiki.html.hbs')).toString('utf8'))(ctx));
fs.writeFileSync('changelog.md', hbs.compile(fs.readFileSync(path.resolve(me, 'changelog.md.hbs')).toString('utf8'))(ctx));
