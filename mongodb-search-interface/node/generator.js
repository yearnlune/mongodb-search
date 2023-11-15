const {GraphQLDefinitionsFactory} = require('@nestjs/graphql');
const fs = require('fs');
const path = require('path');

const graphqlResourceDirectory = path.join(process.cwd(), '../build/resources/main/graphql')
const autoGenerateTypescriptFile = path.join(process.cwd(), 'src/graphql.ts');
const outputGraphqlDirectory = path.join(process.cwd(), 'graphql')

copyRecursiveSync(graphqlResourceDirectory, outputGraphqlDirectory)

const definitionsFactory = new GraphQLDefinitionsFactory();

definitionsFactory.generate({
  typePaths: [`${graphqlResourceDirectory}/*.graphqls`],
  path: autoGenerateTypescriptFile,
  outputAs: 'interface',
  emitTypenameField: true,
  customScalarTypeMapping: {
    Long: 'number',
    JSON: 'any',
    Property: 'string'
  },
  federation: false,
  enumsAsTypes: true
});

function copyRecursiveSync(src, dest) {
  const exists = fs.existsSync(src);
  const stats = exists && fs.statSync(src);
  const isDirectory = exists && stats.isDirectory();
  if (isDirectory) {
    if (!fs.existsSync(dest) || !fs.statSync(dest).isDirectory()) {
      fs.mkdirSync(dest)
    }
    fs.readdirSync(src).forEach(function (childItemName) {
      copyRecursiveSync(path.join(src, childItemName),
              path.join(dest, childItemName));
    });
  } else {
    fs.copyFileSync(src, dest);
  }
}
