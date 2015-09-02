bfr_model_plotter = function() {

	var modelPlotter = {
			version: "1.0.0"
	};
	modelPlotter.name = "Model Plotter";
	
	var plotterValue;
	var plotterRep;
	var variableSliders;
	var functionGraph;
	var jsxBoard;
	
	modelPlotter.init = function(representation, value) {
		//alert(JSON.stringify(representation));
		plotterValue = value;
		plotterRep = representation;
		
		var body = document.getElementsByTagName("body")[0];
		body.setAttribute("style", 
				"width:100%; height:100%; font-family:Arial,Helvetica,sans-serif; font-size:14px;");
		
		var table = document.createElement("table");
		var row1 = document.createElement("tr");
		var td1 = document.createElement("td");
		row1.appendChild(td1);
		var row2 = document.createElement("tr")
		var td2 = document.createElement("td");
		td2.setAttribute("align", "right");
		row2.appendChild(td2);
		table.appendChild(row1);
		table.appendChild(row2);
		body.appendChild(table);
		
		var layoutContainer = document.createElement("div");
		layoutContainer.setAttribute("style", "width:810px; height:660px;");
		td1.appendChild(layoutContainer);
		
		var constantsBox = document.createElement("div");
		constantsBox.setAttribute("style", "border-style:solid; border-width:1px; width:35%; text-align:left; padding:10px;");
		td2.appendChild(constantsBox);
		
		var constantsHeadline = document.createElement("h3");
		constantsHeadline.innerHTML = "Model constants";
		constantsHeadline.setAttribute("style", "margin:5px;");
		constantsBox.appendChild(constantsHeadline);
		
		var selectBox = document.createElement("select");
		selectBox.setAttribute("id", "funcConstants");
		selectBox.setAttribute("style", "margin:5px;");
		selectBox.addEventListener("change", function() { constantSelected(); });
		constantsBox.appendChild(selectBox);
		
		var textInput = document.createElement("input");
		textInput.setAttribute("id", "funcConstantValue");
		textInput.setAttribute("type", "text");
		textInput.setAttribute("size", "7");
		textInput.setAttribute("style", "margin:5px;");
		textInput.addEventListener("change", function() { checkNumberInput(); });
		constantsBox.appendChild(textInput);
		
		var applyButton = document.createElement("button");
		applyButton.innerHTML = "Apply";
		applyButton.setAttribute("id", "funcConstantValueChange");
		applyButton.setAttribute("style", "margin:5px;");
		applyButton.addEventListener("click", function() { applyNewValue(); });
		constantsBox.appendChild(applyButton);
		
		var h = document.createElement("h1");
		h.innerHTML = plotterRep.chartTitle;
		layoutContainer.appendChild(h);
		
		var div = document.createElement("div");
		div.setAttribute("id", "box");
		div.setAttribute("class", "jxgbox");
		div.setAttribute("style", "width:800px; height:600px;");
		layoutContainer.appendChild(div);
		
	    // add constants in view representation to select box as options
	    var constantsSelectBox = document.getElementById('funcConstants');
		for (var c in plotterRep.constants) {
		    var opt = document.createElement('option');
			opt.value = c;
			opt.innerHTML = c;
			constantsSelectBox.appendChild(opt);
		}
		
		// set value in text field to value of selected constant
		constantSelected();
		
		// Initialize JSX board to draw function graphs, etc.
		var minXAxis = plotterRep.minXAxis;
		var maxXAxis = plotterRep.maxXAxis;
		var minYAxis = plotterRep.minYAxis;
		var maxYAxis = plotterRep.maxYAxis;		
		jsxBoard = JXG.JSXGraph.initBoard('box', {boundingbox: [minXAxis, maxYAxis, maxXAxis, minYAxis], axis:false});
		
		var xunit = "[" + plotterRep.xUnit + "]";
		var yunit = "[" + plotterRep.yUnit + "]";
		
		// Set Y axis, ticks, labels
		var yaxis = jsxBoard.create('axis', [[0, 0], [0, 1]], {name:yunit, withLabel: true, 
				ticks: {insertTicks: true, ticksDistance: 1, label: {offset: [-20, -20]}}});
			 yaxis.defaultTicks.ticksFunction = function () { return 5; };

	    // Set X axis, ticks, labels
	    var xaxis = jsxBoard.create('axis', [[0, 0], [1, 0]], {name:xunit, withLabel: true, 
				ticks: {insertTicks: true, ticksDistance: 1, label: {offset: [-20, -20]}}});
			 xaxis.defaultTicks.ticksFunction = function () { return 5; };
			 
		variableSliders = [];
		// Append one slider for each function variable		
		for (var i = 0; i < plotterRep.variables.length; i++) {
			var v = plotterRep.variables[i];
			if (v.name != 'Time') {				
				variableSliders.push(jsxBoard.create('slider', [[50,-2 - i], [80,-2 - i], [v.min, v.def, v.max]],
						{name:v.name, point1: {frozen: true}, point2: {frozen: true}}));
			}
		}		
				
		plotterRep.constants.Y0 = plotterRep.y0;
		
		// Creates time based function and updates plottable model graph 
		updateFunctionGraph();
		
		// On zoom change event remove current function graph and create/draw new function graph 
		// with new max value of x axis
		jsxBoard.on('boundingbox', function() {
			maxXValue = jsxBoard.plainBB[2] + 10;
			jsxBoard.removeObject(functionGraph);
			functionGraph = jsxBoard.create('functiongraph', [timeModelFunction, 0, maxXValue]);		
		});		
		
		// Auto resize view when shown in WebPortal
		if (parent != undefined && parent.KnimePageLoader != undefined) {
			   parent.KnimePageLoader.autoResize(window.frameElement.id);
		}
	}
	
	// read selected constant from select box and show value of constant in text field 
	constantSelected = function() {
		var constantsSelectBox = document.getElementById('funcConstants');
		var constant = constantsSelectBox.options[constantsSelectBox.selectedIndex].value;
		document.getElementById('funcConstantValue').value = plotterRep.constants[constant];
	}
	
	// checks if input of text field is number
	checkNumberInput = function() {
		var constantsSelectBox = document.getElementById('funcConstants');
		var constant = constantsSelectBox.options[constantsSelectBox.selectedIndex].value;
		var newValueField = document.getElementById('funcConstantValue');
		var newValue = newValueField.value;
		
		var newNumberValue = parseFloat(newValue);
		if (isNaN(newNumberValue)) {
			newValueField.style.background = "red";
		} else {
			newValueField.style.background = "white";
		}
	}
	
	applyNewValue = function() {	
		var constantsSelectBox = document.getElementById('funcConstants');
		var constant = constantsSelectBox.options[constantsSelectBox.selectedIndex].value;
		var newValue = document.getElementById('funcConstantValue').value;
		
		var newNumberValue = parseFloat(newValue);
		if (!isNaN(newNumberValue)) {
			plotterRep.constants[constant] = newNumberValue;			
			jsxBoard.removeObject(functionGraph);
			updateFunctionGraph();	
		}
	}
	
	function createFunctionStr() {
		var functionStr = 'f(Time';
		// Append variable name to function string, separated by ","		
		for (var i = 0; i < plotterRep.variables.length; i++) {
			var v = plotterRep.variables[i];
			if (v.name != 'Time') {				
				functionStr += ", " + v.name;
			}
		}
		// Close parameter brackets of function and add function term
		functionStr += ") = " + plotterRep.func;
		
		return functionStr;
	}
	
	function createMath() {
		// Note that due to math.js the root math object can be either "mathjs" or "math",
		// which depends on the used browser.		
		var myMath;
		if (typeof define === 'function' && define.amd) {
			myMath = mathjs;
		} else {
			myMath = math;
		}
		
		// Set all constants of the given model representation
		myMath.import(plotterRep.constants, { override: true });
		
		// CUSTOMIZED FUNCTIONS
		myMath.import({
		  ln: function (x) { return myMath.log(x); },
		  log10: function (x) { return myMath.log(x)/myMath.log(10); }
		});
		
		return myMath;
	}
	
	function createTimeBasedModelFunction(baseModelFunction) {
		var timeModelFunction = function(Time){
		 	var varValues = [Time];
		 	for (var i = 0; i < variableSliders.length; i++) {
		 		varValues.push(variableSliders[i].Value());
		 	}		 
		 	return baseModelFunction.apply(null, varValues);
	 	 };	
	 	 return timeModelFunction;
	}
	
	function updateFunctionGraph() {
		// Save max value of x axis
		var maxXValue = jsxBoard.attr.boundingbox[2] + 10;
		
		// Prepare string of function f of time
		var functionStr = createFunctionStr();
		
		// Prepare myMath object to call math functions 		
		var myMath = createMath();
		
		// Create js function from function string with time and all given parameters as input
		// variables of the function
		var baseModelFunction = myMath.eval(functionStr);
		
		// Create js function ONLY WITH TIME as input variable of the function. As values of all
		// other input variables the slider values are set
		var timeModelFunction = createTimeBasedModelFunction(baseModelFunction);
			
	 	// Create graph of function with time parameter only
		functionGraph = jsxBoard.create('functiongraph', [timeModelFunction, 0, maxXValue]);
	}
	
	modelPlotter.validate = function() {
		return true;
	}
	
	modelPlotter.setValidationError = function() { }
	
	modelPlotter.getComponentValue = function() { 
		return plotterValue;
	}
	
	return modelPlotter;	
}();

