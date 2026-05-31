import fs from "fs";
import path from "path";
import { fileURLToPath } from "url";
import { Resvg } from "@resvg/resvg-js";

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const root = path.resolve(__dirname, "..");
const sourceDir = path.join(root, "source");

function renderSvg(svgPath, outPath, size) {
  const svg = fs.readFileSync(svgPath, "utf8");
  const resvg = new Resvg(svg, {
    fitTo: { mode: "width", value: size },
    background: "transparent",
  });
  const png = resvg.render().asPng();
  fs.mkdirSync(path.dirname(outPath), { recursive: true });
  fs.writeFileSync(outPath, png);
  return png.length;
}

function renderSvgIntrinsic(svgPath, outPath) {
  const svg = fs.readFileSync(svgPath, "utf8");
  const resvg = new Resvg(svg, { background: "transparent" });
  const png = resvg.render().asPng();
  fs.mkdirSync(path.dirname(outPath), { recursive: true });
  fs.writeFileSync(outPath, png);
  return png.length;
}


const outputs = [];

const combined = path.join(sourceDir, "ic_launcher_combined.svg");
const appLogo = path.join(sourceDir, "ic_app_logo.svg");

outputs.push(["play-store-icon-512.png", combined, 512, path.join(root, "play-store-icon-512.png")]);
outputs.push(["launcher-adaptive-1024.png", combined, 1024, path.join(root, "launcher-adaptive-1024.png")]);
outputs.push(["marketing-app-logo-1024.png", appLogo, 1024, path.join(root, "marketing-app-logo-1024.png")]);
outputs.push(["marketing-app-logo-512.png", appLogo, 512, path.join(root, "marketing-app-logo-512.png")]);

const mipmaps = [
  ["mipmap-mdpi", 48],
  ["mipmap-hdpi", 72],
  ["mipmap-xhdpi", 96],
  ["mipmap-xxhdpi", 144],
  ["mipmap-xxxhdpi", 192],
];

for (const [folder, size] of mipmaps) {
  outputs.push([
    `${folder}/ic_launcher.png`,
    combined,
    size,
    path.join(root, "mipmap-export", folder, "ic_launcher.png"),
  ]);
}

for (const [label, svg, size, out] of outputs) {
  renderSvg(svg, out, size);
  console.log(`OK ${label} -> ${out} (${size}x${size})`);
}


const featureSvg = path.join(sourceDir, "play-store-feature-graphic.svg");
const featureOut = path.join(root, "play-store-feature-graphic-1024x500.png");
const featureBytes = renderSvgIntrinsic(featureSvg, featureOut);
console.log(`OK play-store-feature-graphic-1024x500.png -> ${featureOut} (1024x500, ${featureBytes} bytes)`);
