function createParameter($, element, labelField, autocompleteValues, errorHandler) {
	if (autocompleteValues.indexOf("ERROR:")>=0)
		return errorHandler(autocompleteValues);

	labelField = labelField.trim();
    var engineValues = []; 
    autocompleteValues.forEach(function(v) {
    	if (typeof v === 'object') {
    		var l = v[labelField];
    		if (labelField[0] == '{') {
    			var expr = labelField.substring(1,labelField.length-1)
    			expr = expr.replace(/[$]([a-zA-Z][a-zA-Z0-9$]*)/g,"v['$1']")
    			l = eval(expr);
    		}
    		engineValues.push({label: l, value: JSON.stringify(v)});
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
