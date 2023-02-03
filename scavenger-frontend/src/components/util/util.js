export function getFilePath(signature) {
  // if method
  if (signature.endsWith(")")) {
    const lastDotIndex = signature.split("(")[0].lastIndexOf(".");

    signature = signature.substring(0, lastDotIndex);
  }

  const indexOfFirstUpperCase = getIndexOfFirstUpperCase(signature);

  if (indexOfFirstUpperCase === -1) {
    return signature;
  }

  const dotAfterClass = signature.indexOf(".", indexOfFirstUpperCase);

  if (dotAfterClass > 0) {
    signature = signature.slice(0, dotAfterClass);
  }
  return signature;
}

import axios from 'axios';

export function openLink(signature) {
  const filename = `${signature.split(".").join("/")}.java`;
  const url = `http://localhost:63342/api/file/${filename}`;

  return axios.get(url);
}

export function getIndexOfFirstUpperCase(str) {
  return str.split("")
    .findIndex(character =>
      character === character.toUpperCase() &&
      character !== character.toLowerCase(),
    );
}

export function toPercentageStr(float) {
  return Math.floor(float * 100);
}

export function getGradientColor(startColor, endColor, percent) {
  startColor = startColor.replace(/^\s*#|\s*$/g, "");
  endColor = endColor.replace(/^\s*#|\s*$/g, "");

  const startRed = parseInt(startColor.substr(0, 2), 16),
    startGreen = parseInt(startColor.substr(2, 2), 16),
    startBlue = parseInt(startColor.substr(4, 2), 16);

  const endRed = parseInt(endColor.substr(0, 2), 16),
    endGreen = parseInt(endColor.substr(2, 2), 16),
    endBlue = parseInt(endColor.substr(4, 2), 16);

  let diffRed = endRed - startRed;
  let diffGreen = endGreen - startGreen;
  let diffBlue = endBlue - startBlue;

  diffRed = ((diffRed * percent) + startRed).toString(16)
    .split(".")[0];
  diffGreen = ((diffGreen * percent) + startGreen).toString(16)
    .split(".")[0];
  diffBlue = ((diffBlue * percent) + startBlue).toString(16)
    .split(".")[0];

  if (diffRed.length === 1) diffRed = `0${diffRed}`;
  if (diffGreen.length === 1) diffGreen = `0${diffGreen}`;
  if (diffBlue.length === 1) diffBlue = `0${diffBlue}`;

  return `#${diffRed}${diffGreen}${diffBlue}`;
}
