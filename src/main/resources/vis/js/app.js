var app = function() {

  var ws;
  var queries = function() {
    var ensurePopulated = function(query, dimension, measure, attribute) { 
      if (query.queryState[dimension] === undefined)
        query.queryState[dimension] = {};

      if (query.queryState[dimension][measure] === undefined)
        query.queryState[dimension][measure] = {};

      if (query.queryState[dimension][measure][attribute] === undefined)
        query.queryState[dimension][measure][attribute] = {};
    };
    
    var pruneExpiredObjects = function(query, windowLength) {
      // Remove keys that have fallen outside our window.
      var dimensions = Object.keys(query.queryState);
      for (var dimIdx in dimensions) {
        var dimension = query.queryState[dimensions[dimIdx]];
        var measures = Object.keys(dimension);

        for (var measureIdx in measures) {
          var measure = dimension[measures[measureIdx]];

          var attributes = Object.keys(measure);
          for (var attrIdx in attributes) {
            var attrName = attributes[attrIdx];
            var attr = measure[attrName];

            var removedKeys = 0;
            var timestamps = Object.keys(attr).sort();
            while (Object.keys(attr).length > windowLength) {
              delete attr[timestamps[removedKeys]];
              removedKeys += 1;
            }
          }
        }
      }
    };
    
    return {
      ensurePopulated: ensurePopulated,
      pruneExpiredObjects: pruneExpiredObjects
    }
  }();
  
  var makeQuery = function() {
    var queryState = {};
    return {
      queryState: queryState
    }
  };
  
  var makeChart = function(windowLength, domId, pathNames) {
    var paths = {};

    var margin = {top: 0, right: 0, bottom: 0, left: 0};
    var width = 960 - margin.left - margin.right;
    var height = 500 - margin.top - margin.bottom;

    var x = d3.scale.linear().
      domain([1, windowLength - 2]).
      range([0, width]);
 
    var y = d3.scale.linear().
      domain([0, 1]).
      range([height, 0]);

    var line = d3.svg.line().
      interpolate("basis").
      x(function(d, i) { return x(i); }).
      y(function(d, i) { return y(d); });

    var svg = d3.select("#" + domId).append("svg").
      attr("width", width + margin.left + margin.right).
      attr("height", height + margin.top + margin.bottom).
      append("g").
      attr("transform", "translate(" + margin.left + "," + margin.top + ")");

    svg.append("defs").append("clipPath").
      attr("id", "clip").
      append("rect").
      attr("width", width).
      attr("height", height);

    svg.append("g").
      attr("class", "x axis").
      attr("transform", "translate(0," + y(0) + ")").
      call(d3.svg.axis().scale(x).orient("top"));

    var yAxis = d3.svg.axis().scale(y).ticks(7).orient("right");

    svg.append("g").
      attr("class", "y axis").
      call(yAxis);

    for (var pathIdx in pathNames) {
      var pathName = pathNames[pathIdx];
      var data = [];
      paths[pathName] = {
        graphData: data,
        path: svg.append("g").
          attr("clip-path", "url(#clip)").
          append("path").
          datum(data).
          attr("class", "line " + pathName).
          attr("d", line)
        }
    }

    var updatePath = function(myPath) {
      // Scale and transition our y axis as values change.
      y.domain([0, d3.max(paths['max'].graphData, function(d) { return d; })]).nice();
      svg.select(".y.axis").transition().call(yAxis);
      
      // Request path transition.
      myPath.path.attr("d", line)
          .attr("transform", null)
          .transition()
          .duration(500)
          .ease("linear")
          .attr("transform", "translate(" + x(0) + ",0)");
          
      myPath.graphData.shift();
    }

      return {
        svg: svg,
        line: line,
        yAxis: yAxis,
        paths: paths,
        updatePath: updatePath,
        windowLength: windowLength
      }
  };
  
  var initializeEventListeners = function() {
    $("a.label").click(function() {
      var el = $(this);
      el.toggleClass("active");
      var pathEl = $("path." + el.attr('rel'));
      el.hasClass("active") ? pathEl.fadeIn() : pathEl.fadeOut();
    });
    
    $("#measure").change(function() {
      console.log($('#measure').val());
      ws.close();
      launch(true);
    });

    $("#dimension").change(function() {
      console.log($('#dimension').val());
      ws.close();
      launch(true);
    });

  };

  var launch = function(initialized) {
    $("#mainChart").html("");
    var MEASURE = $('#measure').val();
    var DIM = $('#dimension').val();
    $("td.title").html($('#' + MEASURE).html() + " - " + DIM);

    if (!initialized)
      initializeEventListeners();
    
    var attributes = ['min', 'mean', 'median', 'p95', 'p98', 'p99', 'max'];
    var chart = makeChart(10, 'mainChart', attributes);
    
    ws = new WebSocket("ws://localhost:8080/websocket");
    ws.onopen = function(event) { console.log("Connected to websocket endpoint!"); }      
    ws.onmessage = function(event) { handleMessage(JSON.parse(event.data)); }
    ws.onclose = function(event) { console.log("Websocket connection closed."); }

    var query = makeQuery();

    var handleMessage = function(parsed) {
      console.log(parsed);

      queries.pruneExpiredObjects(query, chart.windowLength);

      for (var attr in attributes) {
        var ATTRIBUTE = attributes[attr];
        queries.ensurePopulated(query, DIM, MEASURE, ATTRIBUTE);

        // Insert the data we've received into our query state.
        for (var i in parsed.insert) {
          var timeSlice = parsed.insert[i];
          console.log(timeSlice)
          query.queryState[DIM][MEASURE][ATTRIBUTE][i] =
            timeSlice[DIM][MEASURE][ATTRIBUTE];
        }

        // Transform our query state into graph data and draw.
        var path = chart.paths[ATTRIBUTE];
        var sortedKeys = Object.keys(query.queryState[DIM][MEASURE][ATTRIBUTE]).sort();
        path.graphData.length = 0;
        var padLength = chart.windowLength - sortedKeys.length;
        if (padLength > 0)
          for (var i = 0; i < padLength; i++) path.graphData.push(0);

        for (var key in sortedKeys) {
          path.graphData.push(query.queryState[DIM][MEASURE][ATTRIBUTE][sortedKeys[key]]);
        }
  
        // Request that our path transition to the state of our new data.
        chart.updatePath(path);
      }
    }
  }
  
  
  return {
    launch:launch
  }

}();
