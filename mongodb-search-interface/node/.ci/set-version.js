const fs = require('fs');

const packageJsonFile = 'package.json'
const arg = process.argv[2]
let version = '';

if (arg.match('v\\d+\\.\\d+\\.\\d+|v\\d+\\.\\d+\\.\\d+-rc\\d+')) {
  version = arg.substring(1)
} else if (arg.match('\\d+\\.\\d+\\.\\d+|v\\d+\\.\\d+\\.\\d+-rc\\d+')) {
  version = arg
} else {
  console.error(`INVALID VERSION ${arg}`)
  process.exit(1)
}

const packageJson = JSON.parse(fs.readFileSync(packageJsonFile, { encoding: "utf-8"}))
packageJson.version = version
fs.writeFileSync(packageJsonFile, JSON.stringify(packageJson, null, 2));

process.exit(0)
