//
// See https://github.com/tfennelly/jenkins-js-builder
//
var builder = require('jenkins-js-builder');

//
// Bundle the modules.
// See https://github.com/tfennelly/jenkins-js-builder#bundling
//
builder.bundle('src/main/js/parameter-groovy-editor.js')
    .withExternalModuleMapping('jqueryui-detached', 'jquery-detached:jqueryui1')
    .inDir('target/generated-resources/adjuncts/org/jenkinsci/plugins/autocompleteparameter');

builder.defineTask('lint', function() {});
