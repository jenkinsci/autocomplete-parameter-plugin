function getFirstValue(obj) {
    for (var i in obj)
        return obj[i];
}

function evaluateExpression(expression, bindings, errorHandler)
{
    try {
        var v = bindings;
        var script = [];
        script.push("(function(){");
        for (var key in v) {
            var value=v[key];
            script.push("var " + key+"='"+value.replace(/'/g,"\\'")+"';");
        }
        var expr = expression.substr(1,expression.length-2);
        if (expr.trim().length == 0) {
            if (Object.keys(bindings).length == 1)
                return getFirstValue(bindings);
            return JSON.stringify(bindings);
        }
        script.push("return " + expr);
        script.push("})()");
        return eval(script.join(""));
    }catch(e) {
        errorHandler("Failure evaluating expression : '"+expression+"' "+ e.message);
    }
}

function createSelect2($, $element, displayExpression, valueExpression, dropdownValues, defaultValue, prefetch, dropdown, errorHandler) {
  if (dropdownValues.indexOf("ERROR:")>=0)
    return errorHandler(dropdownValues);

  var config = {
    placeholder: 'Select an option'
  };
  if (prefetch) {
    var data = transformValues(dropdownValues, displayExpression, valueExpression, errorHandler);
    config.data = $.map(data, function(item) {
      return {id: item.value, text: item.label};
    });
  } else {
    var loadingImg;
    function addLoadingIcon() {
      var select2Container = $element.parent().find('.select2-container');
      var loadingX = select2Container.width() + 3;
      loadingImg = $('<img style="position:absolute; left:' + loadingX + 'px; top:5px;">').attr('src', rootURL + '/plugin/autocomplete-parameter/ic_loading.gif');
      loadingImg.appendTo(select2Container);
    }
    function removeLoadingIcon() {
      $(loadingImg).remove();
      loadingImg = null;
    };
    config.minimumInputLength = 1;
    config.ajax = {
      transport: function(params, success, failure) {
        removeLoadingIcon();
        addLoadingIcon();
        var request = {};
        var query = params.data.term === undefined ? '' : params.data.term;
        dropdown.filterAutoCompleteValues(query, function(t) {
          removeLoadingIcon();
          if(t.responseJSON.indexOf("ERROR:")>=0) {
            failure();
          } else {
            success({ results: transformValues(JSON.parse(t.responseJSON), displayExpression, valueExpression, errorHandler) });
          }
        });
        return request;
      },
      processResults: function (data) {
        return {
          results: $.map(data.results, function(item) {
            return {id: item.value, text: item.label};
          })
        }
      },
      delay: 350
    }
  }
  $element.select2(config);
  $('.select2-container').css({"min-width": "200px"});
  if(!prefetch) {
    $element.on('select2:close', function(e) {
      removeLoadingIcon();
    });
  }
  if(defaultValue) {
    $element.val(defaultValue);
    $element.trigger('change');
  }
}

function transformValues(rawValues, displayExpression, valueExpression, errorHandler) {
  var transformed = [];
  rawValues.forEach(function(v) {
    if (typeof v !== 'object') {
      v = {value: v}
    }
    var l = JSON.stringify(v);
    if (displayExpression[0] == '{')
      l = evaluateExpression(displayExpression, v, errorHandler);
    var value = evaluateExpression(valueExpression, v, errorHandler);
    transformed.push({label: l, value: value});
  });
  return transformed;
}

function filterAlreadySelected(element, suggestions) {
  var tokens = element.tokenfield("getTokens");
  var s = [];
  suggestions.forEach(function(sugg) {
    var exists = false;
    tokens.forEach(function(t) {
      exists=exists || (sugg.label == t.label)
    });
    if (!exists)
      s.push(sugg);
  });
  return s;
}

function createParameter($, element, displayExpression, valueExpression, autocompleteValues, allowUnrecognizedTokens, prefetch, autocomplete, errorHandler) {
  if (autocompleteValues.indexOf("ERROR:")>=0)
    return errorHandler(autocompleteValues);
        
  if (typeof allowUnrecognizedTokens == 'undefined')
    allowUnrecognizedTokens = false;

  displayExpression = displayExpression.trim();

  var source;
  var validate;
  if(prefetch) {
    var engineValues = transformValues(autocompleteValues, displayExpression, valueExpression, errorHandler);
    var engine = new Bloodhound({
      local: engineValues,
      datumTokenizer: function(d){
        var tokens = [];
        var stringSize = d.label.length;
        for (var size = 1; size <= stringSize; size++){
          for (var i = 0; i+size<= stringSize; i++){
            tokens.push(d.label.substr(i, size));
          }
        }

        return tokens;
      },
      queryTokenizer: Bloodhound.tokenizers.whitespace,
      limit: autocompleteValues.length
    });

    engine.initialize();

    source = function(query, callback) {
      engine.get(query, function(suggestions) {
        callback(filterAlreadySelected(element, suggestions).slice(0, 10));
      });
    };
    validate = function(e) {
      if(allowUnrecognizedTokens)
        return true;
      var isValid = false;
      engine.local.forEach(function(l) {
        if (e.attrs.label.toLowerCase() == l.label.toLowerCase())
          isValid = true;
      });
      return isValid;
    };
  } else {
    var filterTimeout;
    var suggestions = [];
    var loadingImg;
    function addLoadingIcon() {
      var input = element.data('bs.tokenfield').$input;
      var txt = input[0].value;
      var fakeEl = $('<span>').hide().appendTo(document.body).text(txt).css({font: input.css('font'), whiteSpace: "pre"});
      var textWidth = fakeEl.width();
      fakeEl.remove();

      var loadingX = textWidth + 13;
      loadingImg = $('<img style="position:absolute; left:' + loadingX + 'px; top:8px;">').attr('src', rootURL + '/plugin/autocomplete-parameter/ic_loading.gif');
      loadingImg.appendTo(element.parent().find('.twitter-typeahead'));
    }
    function removeLoadingIcon() {
      $(loadingImg).remove();
      loadingImg = null;
    }
    source = function(query, callback) {
      removeLoadingIcon();
      clearTimeout(filterTimeout);
      filterTimeout = setTimeout(function() {
        addLoadingIcon();
        autocomplete.filterAutoCompleteValues(query, function(t) {
          removeLoadingIcon();
          suggestions = transformValues(JSON.parse(t.responseJSON), displayExpression, valueExpression, errorHandler);
          callback(filterAlreadySelected(element, suggestions).slice(0, 10));
          setTimeout(function() {
            // make sure hint isn't clipped
            element.tokenfield('update');
          });
        });
      }, 350);
    };
    validate = function(e) {
      if(suggestions && suggestions.constructor === Array) {
        if(allowUnrecognizedTokens)
          return true;
        var isValid = false;
        suggestions.forEach(function(l) {
          if (e.attrs.label.toLowerCase() == l.label.toLowerCase())
            isValid = true;
        });
        return isValid;
      }
    };
  }

  element.tokenfield({
    typeahead: [null, {
      display: function(d) {
          return d.label;
      },
      source: source
    }]
  });

  element.on("tokenfield:createtoken", function(e) {
    var isValid = validate(e);
    if (!isValid)
      return false;
    var exists = false;
    $(this).tokenfield('getTokens').forEach(function(token) {
      if (e.attrs.label == token.label)
        exists = true;
    });
    if (exists)
      return false;
  });

  function validateLeftovers(input) {
    clearValidateLeftovers(input);
    if(input.value) {
      $(input).addClass('dotted-error');
      errorHandler('<span class="warning">The unrecognized token "' + input.value + '" will be ignored.</span>')
    }
  }
  function clearValidateLeftovers(input) {
    errorHandler("");
    $(input).removeClass('dotted-error');
  }

  element.data('bs.tokenfield').$input.on('focus', function(ex) {
    clearValidateLeftovers(this);
  });
  element.data('bs.tokenfield').$input.on('blur', function(ev) {
    validateLeftovers(this);
  });
}
