    var qd = {};
    location.search.substr(1).split("&").forEach(function(item) {
        var s = item.split("="),
            k = s[0],
            v = s[1] && decodeURIComponent(s[1]);
        (k in qd) ? qd[k].push(v) : qd[k] = [v]
    });

    function toggleNode(url) {
        $.ajax({
            type: 'POST',
            url: url,
            headers: {
                authorization: 'Basic ' + btoa(qd['auth'])
            },
            success: function(data) {
                // Do nothing
            },
            error: function() {
                alert("Could not start node!");
            }
        });
    }

    Highcharts.setOptions({ // This is for all plots, change Date axis to local timezone
        global : {
            useUTC : false
        }
    });
    var charts = {};

    function loadChart(id) {
        if (!(id in charts)) {
            var chart = new Highcharts.Chart({
                chart: {
                    renderTo: 'chart' + id,
                    type: 'spline',
                    animation: false,// Highcharts.svg, // don't animate in old IE
                    marginRight: 10,
                    backgroundColor: 'rgba(0, 0, 0, 0.1)'
                },
                title: {
                    text: null
                },
                xAxis: {
                    type: 'datetime',
                    tickPixelInterval: 150
                },
                yAxis: {
                    title: {
                        text: null
                    },
                    min: 0,
                    max: 1,
                    plotLines: [{
                        value: 0,
                        width: 1,
                        color: '#808080'
                    }]
                },
                tooltip: {
                    formatter: function () {
                        return '<b>' + this.series.name + '</b><br/>' +
                            Highcharts.dateFormat('%Y-%m-%d %H:%M:%S', this.x) + '<br/>' +
                            Highcharts.numberFormat(this.y, 2);
                    }
                },
                legend: {
                    enabled: false
                },
                plotOptions: {
                    series: {
                        marker: {
                            enabled: false
                        }
                    }
                },
                exporting: {
                    enabled: false
                },
                series: [{
                    name: 'System Load %',
                    data: [],
                    color: 'red'
                }, {
                    name: 'Process Load %',
                    data: [],
                    color: 'blue'
                }, {
                    name: 'Process Load Applied %',
                    data: [],
                    color: 'green'
                }]
            });
            charts[id] = chart;
        }
        return charts[id];
    }

    var sliderLock = false;

    function setSlider(id, value) {
        if (!sliderLock) {
            document.getElementById('slider' + id).value = value;
            document.getElementById('sliderValue' + id).textContent = "Expected Load: " + value;
        }
    }

    function changeSlider(id, url) {
        sliderLock = true;
        var slider = document.getElementById('slider' + id);
        document.getElementById('sliderValue' + id).textContent = "Expected Load: " + slider.value;
        $.ajax({
            type: 'POST',
            url: url + '?load=' + slider.value,
            headers: {
                authorization: 'Basic ' + btoa(qd['auth'])
            },
            success: function(data) {
                sliderLock = false;
            },
            error: function() {
                alert("Could not adjust expected load!");
                sliderLock = false;
            }
        });
    }

    function loadNodes() {
        $.ajax({
            type: 'GET',
            url: './api/server/nodes',
            headers: {
                authorization: 'Basic ' + btoa(qd['auth'])
            },
            success: function(data) {
                $('#placeholderNode').css('display','none');
                var ids = {};
                data.forEach(function(nodeData) {
                    ids[nodeData.id] = 1;
                    if (document.getElementById('div' + nodeData.id) == null) {
                        var nodeDiv = $(document.createElement('div'));
                        var leftControlDiv = $(document.createElement('div'));
                        leftControlDiv.append('<div style="float:left"><input id="status' + nodeData.id + '" type="image" src="" style="height:40px" /></div>');
                        leftControlDiv.append('<div style="float:right"><a href="' + nodeData.url + '?auth=' + qd['auth'] + '" target="_blank"><span>' + nodeData.id.substring(0,8) + '</span></a></div>');
                        leftControlDiv.append('<div style="clear:left;width:100%">' +
                            '<input id="slider' + nodeData.id + '" type="range" min="0" max="1" step="0.01" value="' + nodeData.status.expectedLoad + '" style="width:100%" onchange="changeSlider(\'' + nodeData.id + '\',\'' + nodeData.url + '/api/node/set' + '\')"/>' +
                            '<div style="float:left">' +
                            '<span id="sliderValue' + nodeData.id + '" style="width:40px;margin:10px 10px 10px 10px;padding:5px;vertical-align:middle">Expected Load: ' + nodeData.status.expectedLoad + '</span>' +
                            '</div>');
                        leftControlDiv.css('float','left');
                        leftControlDiv.css('height','200px');
                        leftControlDiv.css('min-width','300px');
                        leftControlDiv.css('width','48%');
                        leftControlDiv.css('margin','0');
                        leftControlDiv.css('padding','10px');
                        leftControlDiv.css('display','inline-block');
                        nodeDiv.append(leftControlDiv);
                        nodeDiv.append('<div id="chart' + nodeData.id + '" style="min-width:300px;height:200px;float:left;display:inline-block;width:48%;margin:10px auto;"></div>');
                        nodeDiv.css('overflow','auto');
                        nodeDiv.css('background','linear-gradient(rgba(255,255,255,0.1), rgba(255,255,255,0.2)');
                        nodeDiv.css('text-align','left');
                        nodeDiv.css('margin','5px');
                        nodeDiv.css('padding','5px');
                        nodeDiv.attr('id',"div" + nodeData.id);
                        nodeDiv.attr('nodeId', nodeData.id);
                        $('#nodes').append(nodeDiv);
                    }
                    var nodeDiv = $('#div' + nodeData.id);
                    var statusDiv = $('#status' + nodeData.id);
                    if (nodeData.status.running && statusDiv.attr("running") != "true") {
                        statusDiv.attr("running","true");
                        statusDiv.attr("src","./images/off.png");
                        statusDiv.unbind("click");
                        statusDiv.click(function(){toggleNode(nodeData.url + "/api/node/stop")});
                    }
                    else if (!nodeData.status.running) {
                        setSlider(nodeData.id, nodeData.status.expectedLoad);
                        if(statusDiv.attr("running") != "false"){
                            statusDiv.attr("running","false");
                            statusDiv.attr("src","./images/on.png");
                            statusDiv.unbind("click");
                            statusDiv.click(function(){toggleNode(nodeData.url + "/api/node/start")});
                        }
                    }
                    var chart = loadChart(nodeData.id);
                    // chart.redraw(false);
                    var shift = chart.series[0].data.length > 60;
                    chart.series[0].addPoint([nodeData.status.load.timestamp, nodeData.status.load.systemLoad],true,shift,false);
                    shift = chart.series[1].data.length > 60;
                    chart.series[1].addPoint([nodeData.status.load.timestamp, nodeData.status.load.processLoad],true,shift,false);
                    shift = chart.series[2].data.length > 60;
                    chart.series[2].addPoint([nodeData.status.load.timestamp, nodeData.status.load.processLoadApplied],true,shift,false);
                    chart.yAxis[0].removePlotLine('expectedLoad');
                    chart.yAxis[0].addPlotLine({
                        value: nodeData.status.expectedLoad,
                        color: 'red',
                        width: 2,
                        id: 'expectedLoad',
                        label: {
                            text: 'Expected CPU Load %',
                            style: {color: 'white'}
                        }
                    });
                });

                // Remove untracked charts here
                $('#nodes').children('div').each(function() {
                    var node = $(this);
                    var nodeId = node.attr('nodeId');
                    if (!(nodeId in ids)) {
                        delete charts[nodeId];
                        node.remove();
                    }
                });
            },
            error: function() {
                $('#placeholderNode').css('display','block');
            },
            cache: false
        });
    }