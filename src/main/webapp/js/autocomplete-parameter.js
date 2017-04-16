function getFirstValue(obj) {
	for (var i in obj) {
		return obj[i];
	}
}

function evaluateExpression(expression, bindings, errorHandler)
{
	try {
		var v = bindings;
		for (key in v) {
			value=v[key];
			eval("var " + key+"='"+value.replace(/'/g,"\\'")+"'");
		}
		var expr = expression.substr(1,expression.length-2);
		if (expr.trim().length == 0) {
			if (Object.keys(bindings).length == 1)
				return getFirstValue(bindings);
			return JSON.stringify(bindings);
		}
		
		return eval(expr);
	}catch(e) {
		errorHandler("Failure evaluating expression : '"+expression+"' "+ e.message);
	}
}

function convertToChosen($, $element, displayExpression, valueExpression, errorHandler)
{
	$element.find("option").each(function(i, option) {
		var bindings;
		try {
			bindings = JSON.parse(option.value);
		}catch(e) {
			// value is not json, probably scalar value
			bindings = {value:option.value}
		}

		option.innerHTML = evaluateExpression(displayExpression, bindings, errorHandler);
		option.value = evaluateExpression(valueExpression, bindings, errorHandler);		
	});
	$element.chosen();
	$('.chosen-container').css({"min-width": "200px"});	
}

function createParameter($, element, displayExpression, valueExpression, autocompleteValues, allowUnrecognizedTokens, errorHandler) {
	if (autocompleteValues.indexOf("ERROR:")>=0)
		return errorHandler(autocompleteValues);
		
	if (typeof allowUnrecognizedTokens == 'undefined')
		allowUnrecognizedTokens = false;

	displayExpression = displayExpression.trim();
    var engineValues = []; 
    autocompleteValues.forEach(function(v) {
    	if (typeof v === 'object') {
    		var l = JSON.stringify(v);
    		if (displayExpression[0] == '{') 
    			l = evaluateExpression(displayExpression, v, errorHandler);
    		var value = evaluateExpression(valueExpression, v, errorHandler);
    		
    		engineValues.push({label: l, value: value});
    	}
    	else
    		engineValues.push({label: v, value:v});
    })
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
	  queryTokenizer: Bloodhound.tokenizers.whitespace
	});

	engine.initialize();

	element.tokenfield({
	  typeahead: [null, { 
	    display: function(d) {
	    	return d.label;
	    },
	    source: // don't suggest values already chosen 
			function(query, cb) {
			    engine.get(query, function(suggestions) {
			    tokens = element.tokenfield("getTokens");
			    s = []
			    suggestions.forEach(function(sugg) {
			        var exists = false;
			        tokens.forEach(function(t) {
			            exists=exists || (sugg.label == t.label)
			        });
			        if (!exists)
			            s.push(sugg)
			    })
			    cb(s);
			});
	    }}] 
	});

	if (allowUnrecognizedTokens) return;
	
	element.on("tokenfield:createtoken", function(e) {
	    var isValid =false; 
	    engine.local.forEach(function(l) {
			if (e.attrs.label.toLowerCase() == l.label.toLowerCase())
			    isValid = true;
		    }); 
	    if (!isValid)
			return false;
	    var exists = false;
	    $(this).tokenfield('getTokens').forEach(function(token) {
			if (e.attrs.label == token.label)
			    exists = true;
		}); 
	    if (exists) 
	    	return false;
	})
}
