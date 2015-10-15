/*
# =============================================================================
#   Sparkline Linechart JS
# =============================================================================
*/


(function() {
  var linechartResize;

  linechartResize = function() {
    $("#linechart-1").sparkline([160, 240, 120, 200, 180, 350, 230, 200, 280, 380, 400, 360, 300, 220, 200, 150, 40, 70, 180, 110, 200, 160, 200, 220], {
      type: "line",
      width: "100%",
      height: "226",
      lineColor: "#a5e1ff",
      fillColor: "rgba(241, 251, 255, 0.9)",
      lineWidth: 2,
      spotColor: "#a5e1ff",
      minSpotColor: "#bee3f6",
      maxSpotColor: "#a5e1ff",
      highlightSpotColor: "#80cff4",
      highlightLineColor: "#cccccc",
      spotRadius: 6,
      chartRangeMin: 0
    });
    $("#linechart-1").sparkline([100, 280, 150, 180, 220, 180, 130, 180, 180, 280, 260, 260, 200, 120, 200, 150, 100, 100, 180, 180, 200, 160, 180, 120], {
      type: "line",
      width: "100%",
      height: "226",
      lineColor: "#cfee74",
      fillColor: "rgba(244, 252, 225, 0.5)",
      lineWidth: 2,
      spotColor: "#b9e72a",
      minSpotColor: "#bfe646",
      maxSpotColor: "#b9e72a",
      highlightSpotColor: "#b9e72a",
      highlightLineColor: "#cccccc",
      spotRadius: 6,
      chartRangeMin: 0,
      composite: true
    });
    $("#linechart-2").sparkline([160, 240, 250, 280, 300, 250, 230, 200, 280, 380, 400, 360, 300, 220, 200, 150, 100, 100, 180, 180, 200, 160, 220, 140], {
      type: "line",
      width: "100%",
      height: "226",
      lineColor: "#a5e1ff",
      fillColor: "rgba(241, 251, 255, 0.9)",
      lineWidth: 2,
      spotColor: "#a5e1ff",
      minSpotColor: "#bee3f6",
      maxSpotColor: "#a5e1ff",
      highlightSpotColor: "#80cff4",
      highlightLineColor: "#cccccc",
      spotRadius: 6,
      chartRangeMin: 0
    });
    $("#linechart-3").sparkline([100, 280, 150, 180, 220, 180, 130, 180, 180, 280, 260, 260, 200, 120, 200, 150, 100, 100, 180, 180, 200, 160, 220, 140], {
      type: "line",
      width: "100%",
      height: "226",
      lineColor: "#cfee74",
      fillColor: "rgba(244, 252, 225, 0.5)",
      lineWidth: 2,
      spotColor: "#b9e72a",
      minSpotColor: "#bfe646",
      maxSpotColor: "#b9e72a",
      highlightSpotColor: "#b9e72a",
      highlightLineColor: "#cccccc",
      spotRadius: 6,
      chartRangeMin: 0
    });
    $("#linechart-4").sparkline([100, 220, 150, 140, 200, 180, 130, 180, 180, 210, 240, 200, 170, 120, 200, 150, 100, 100], {
      type: "line",
      width: "100",
      height: "30",
      lineColor: "#adadad",
      fillColor: "rgba(244, 252, 225, 0.0)",
      lineWidth: 2,
      spotColor: "#909090",
      minSpotColor: "#909090",
      maxSpotColor: "#909090",
      highlightSpotColor: "#666",
      highlightLineColor: "#666",
      spotRadius: 0,
      chartRangeMin: 0
    });
    $("#linechart-5").sparkline([100, 220, 150, 140, 200, 180, 130, 180, 180, 210, 240, 200, 170, 120, 200, 150, 100, 100], {
      type: "line",
      width: "100",
      height: "30",
      lineColor: "#adadad",
      fillColor: "rgba(244, 252, 225, 0.0)",
      lineWidth: 2,
      spotColor: "#909090",
      minSpotColor: "#909090",
      maxSpotColor: "#909090",
      highlightSpotColor: "#666",
      highlightLineColor: "#666",
      spotRadius: 0,
      chartRangeMin: 0
    });
    $("#barchart-2").sparkline([160, 220, 260, 120, 320, 260, 300, 160, 240, 100, 240, 120], {
      type: "bar",
      height: "226",
      barSpacing: 8,
      barWidth: 18,
      barColor: "#8fdbda"
    });
    $("#composite-chart-1").sparkline([160, 220, 260, 120, 320, 260, 300, 160, 240, 100, 240, 120], {
      type: "bar",
      height: "226",
      barSpacing: 8,
      barWidth: 18,
      barColor: "#8fdbda"
    });
    return $("#composite-chart-1").sparkline([100, 280, 150, 180, 220, 180, 130, 180, 180, 280, 260, 260], {
      type: "line",
      width: "100%",
      height: "226",
      lineColor: "#cfee74",
      fillColor: "rgba(244, 252, 225, 0.5)",
      lineWidth: 2,
      spotColor: "#b9e72a",
      minSpotColor: "#bfe646",
      maxSpotColor: "#b9e72a",
      highlightSpotColor: "#b9e72a",
      highlightLineColor: "#cccccc",
      spotRadius: 6,
      chartRangeMin: 0,
      composite: true
    });
  };

  $(document).ready(function() {
    /*
    # =============================================================================
    #   Sparkline Linechart JS
    # =============================================================================
    */

    var $alpha, $container, $container2, addEvent, buildMorris, checkin, checkout, d, date, handleDropdown, initDrag, m, now, nowTemp, timelineAnimate, y;
    $("#barcharts").sparkline([190, 220, 210, 220, 220, 260, 300, 220, 240, 240, 220, 200, 240, 260, 210], {
      type: "bar",
      height: "100",
      barSpacing: 4,
      barWidth: 13,
      barColor: "#cbcbcb",
      highlightColor: "#89D1E6"
    });
    $("#pie-chart").sparkline([2, 8, 6, 10], {
      type: "pie",
      height: "220",
      width: "220",
      offset: "+90",
      sliceColors: ["#a0eeed", "#81e970", "#f5af50", "#f46f50"]
    });
    $(".sparkslim").sparkline('html', {
      type: "line",
      width: "100",
      height: "30",
      lineColor: "#adadad",
      fillColor: "rgba(244, 252, 225, 0.0)",
      lineWidth: 2,
      spotColor: "#909090",
      minSpotColor: "#909090",
      maxSpotColor: "#909090",
      highlightSpotColor: "#666",
      highlightLineColor: "#666",
      spotRadius: 0,
      chartRangeMin: 0
    });
    /*
    # =============================================================================
    #   Easy Pie Chart
    # =============================================================================
    */

    $(".pie-chart1").easyPieChart({
      size: 200,
      lineWidth: 12,
      lineCap: "square",
      barColor: "#81e970",
      animate: 800,
      scaleColor: false
    });
    $(".pie-chart2").easyPieChart({
      size: 200,
      lineWidth: 12,
      lineCap: "square",
      barColor: "#f46f50",
      animate: 800,
      scaleColor: false
    });
    $(".pie-chart3").easyPieChart({
      size: 200,
      lineWidth: 12,
      lineCap: "square",
      barColor: "#fab43b",
      animate: 800,
      scaleColor: false
    });
    /*
    # =============================================================================
    #   Navbar scroll animation
    # =============================================================================
    */

    $(".navbar.scroll-hide").mouseover(function() {
      $(".navbar.scroll-hide").removeClass("closed");
      return setTimeout((function() {
        return $(".navbar.scroll-hide").css({
          overflow: "visible"
        });
      }), 150);
    });
    $(function() {
      var delta, lastScrollTop;
      lastScrollTop = 0;
      delta = 50;
      return $(window).scroll(function(event) {
        var st;
        st = $(this).scrollTop();
        if (Math.abs(lastScrollTop - st) <= delta) {
          return;
        }
        if (st > lastScrollTop) {
          $('.navbar.scroll-hide').addClass("closed");
        } else {
          $('.navbar.scroll-hide').removeClass("closed");
        }
        return lastScrollTop = st;
      });
    });
    /*
    # =============================================================================
    #   Mobile Nav
    # =============================================================================
    */

    $('.navbar-toggle').click(function() {
      return $('body, html').toggleClass("nav-open");
    });
    /*
    # =============================================================================
    #   Sparkline Resize Script
    # =============================================================================
    */

    linechartResize();
    $(window).resize(function() {
      return linechartResize();
    });
    /*
    # =============================================================================
    #   Form wizard
    # =============================================================================
    */

    $("#wizard").bootstrapWizard({
      nextSelector: ".btn-next",
      previousSelector: ".btn-previous",
      onNext: function(tab, navigation, index) {
        var $current, $percent, $total;
        if (index === 1) {
          if (!$("#name").val()) {
            $("#name").focus();
            $("#name").addClass("has-error");
            return false;
          }
        }
        $total = navigation.find("li").length;
        $current = index + 1;
        $percent = ($current / $total) * 100;
        return $("#wizard").find(".progress-bar").css("width", $percent + "%");
      },
      onPrevious: function(tab, navigation, index) {
        var $current, $percent, $total;
        $total = navigation.find("li").length;
        $current = index + 1;
        $percent = ($current / $total) * 100;
        return $("#wizard").find(".progress-bar").css("width", $percent + "%");
      },
      onTabShow: function(tab, navigation, index) {
        var $current, $percent, $total;
        $total = navigation.find("li").length;
        $current = index + 1;
        $percent = ($current / $total) * 100;
        return $("#wizard").find(".progress-bar").css("width", $percent + "%");
      }
    });
    /*
    # =============================================================================
    #   DataTables
    # =============================================================================
    */

    $("#dataTable1").dataTable({
      "sPaginationType": "full_numbers",
      aoColumnDefs: [
        {
          bSortable: false,
          aTargets: [0, -1]
        }
      ]
    });
    $('.table').each(function() {
      return $(".table #checkAll").click(function() {
        if ($(".table #checkAll").is(":checked")) {
          return $(".table input[type=checkbox]").each(function() {
            return $(this).prop("checked", true);
          });
        } else {
          return $(".table input[type=checkbox]").each(function() {
            return $(this).prop("checked", false);
          });
        }
      });
    });
    /*
    # =============================================================================
    #   jQuery UI Sliders
    # =============================================================================
    */

    $(".slider-basic").slider({
      range: "min",
      value: 50,
      slide: function(event, ui) {
        return $(".slider-basic-amount").html("$" + ui.value);
      }
    });
    $(".slider-basic-amount").html("$" + $(".slider-basic").slider("value"));
    $(".slider-increments").slider({
      range: "min",
      value: 30,
      step: 10,
      slide: function(event, ui) {
        return $(".slider-increments-amount").html("$" + ui.value);
      }
    });
    $(".slider-increments-amount").html("$" + $(".slider-increments").slider("value"));
    $(".slider-range").slider({
      range: true,
      values: [15, 70],
      slide: function(event, ui) {
        return $(".slider-range-amount").html("$" + ui.values[0] + " - $" + ui.values[1]);
      }
    });
    $(".slider-range-amount").html("$" + $(".slider-range").slider("values", 0) + " - $" + $(".slider-range").slider("values", 1));
    /*
    # =============================================================================
    #   Bootstrap Tabs
    # =============================================================================
    */

    $("#myTab a:last").tab("show");
    /*
    # =============================================================================
    #   Bootstrap Popover
    # =============================================================================
    */

    $("#popover").popover();
    $("#popover-left").popover({
      placement: "left"
    });
    $("#popover-top").popover({
      placement: "top"
    });
    $("#popover-right").popover({
      placement: "right"
    });
    $("#popover-bottom").popover({
      placement: "bottom"
    });
    /*
    # =============================================================================
    #   Bootstrap Tooltip
    # =============================================================================
    */

    $("#tooltip").tooltip();
    $("#tooltip-left").tooltip({
      placement: "left"
    });
    $("#tooltip-top").tooltip({
      placement: "top"
    });
    $("#tooltip-right").tooltip({
      placement: "right"
    });
    $("#tooltip-bottom").tooltip({
      placement: "bottom"
    });
    /*
    # =============================================================================
    #   jQuery VMap
    # =============================================================================
    */

    if ($("#vmap").length) {
      $("#vmap").vectorMap({
        map: "world_en",
        backgroundColor: null,
        color: "#fff",
        hoverOpacity: 0.2,
        selectedColor: "#fff",
        enableZoom: true,
        showTooltip: true,
        values: sample_data,
        scaleColors: ["#59cdfe", "#0079fe"],
        normalizeFunction: "polynomial"
      });
    }
    /*
    # =============================================================================
    #   Full Calendar
    # =============================================================================
    */

    date = new Date();
    d = date.getDate();
    m = date.getMonth();
    y = date.getFullYear();
    initDrag = function(el) {
      /*
      # create an Event Object (http://arshaw.com/fullcalendar/docs/event_data/Event_Object/)
      # it doesn't need to have a start or end
      */

      var eventObject;
      eventObject = {
        title: $.trim(el.text())
      };
      /*
      # store the Event Object in the DOM element so we can get to it later
      */

      el.data("eventObject", eventObject);
      /*
      # make the event draggable using jQuery UI
      */

      return el.draggable({
        zIndex: 999,
        revert: true,
        revertDuration: 0
      });
    };
    addEvent = function(title, priority) {
      var html;
      title = (title.length === 0 ? "Untitled Event" : title);
      priority = (priority.length === 0 ? "default" : priority);
      html = $("<div data-class=\"label label-" + priority + "\" class=\"external-event label label-" + priority + "\">" + title + "</div>");
      jQuery("#event_box").append(html);
      return initDrag(html);
    };
    $("#external-events div.external-event").each(function() {
      return initDrag($(this));
    });
    $("#event_add").click(function() {
      var priority, title;
      title = $("#event_title").val();
      priority = $("#event_priority").val();
      return addEvent(title, priority);
    });
    /*
    # modify chosen options
    */

    handleDropdown = function() {
      $("#event_priority_chzn .chzn-search").hide();
      $("#event_priority_chzn_o_1").html("<span class=\"label label-default\">" + $("#event_priority_chzn_o_1").text() + "</span>");
      $("#event_priority_chzn_o_2").html("<span class=\"label label-success\">" + $("#event_priority_chzn_o_2").text() + "</span>");
      $("#event_priority_chzn_o_3").html("<span class=\"label label-info\">" + $("#event_priority_chzn_o_3").text() + "</span>");
      $("#event_priority_chzn_o_4").html("<span class=\"label label-warning\">" + $("#event_priority_chzn_o_4").text() + "</span>");
      return $("#event_priority_chzn_o_5").html("<span class=\"label label-important\">" + $("#event_priority_chzn_o_5").text() + "</span>");
    };
    $("#event_priority_chzn").click(handleDropdown);
    /*
    # predefined events
    */

    addEvent("My Event 1", "primary");
    addEvent("My Event 2", "success");
    addEvent("My Event 3", "info");
    addEvent("My Event 4", "warning");
    addEvent("My Event 5", "danger");
    addEvent("My Event 6", "default");
    $("#calendar").fullCalendar({
      header: {
        left: "prev,next today",
        center: "title",
        right: "month,agendaWeek,agendaDay"
      },
      editable: true,
      droppable: true,
      drop: function(date, allDay) {
        /*
        # retrieve the dropped element's stored Event Object
        */

        var copiedEventObject, originalEventObject;
        originalEventObject = $(this).data("eventObject");
        /*
        # we need to copy it, so that multiple events don't have a reference to the same object
        */

        copiedEventObject = $.extend({}, originalEventObject);
        /*
        # assign it the date that was reported
        */

        copiedEventObject.start = date;
        copiedEventObject.allDay = allDay;
        copiedEventObject.className = $(this).attr("data-class");
        /*
        # render the event on the calendar
        # the last `true` argument determines if the event "sticks" (http://arshaw.com/fullcalendar/docs/event_rendering/renderEvent/)
        */

        $("#calendar").fullCalendar("renderEvent", copiedEventObject, true);
        /*
        # is the "remove after drop" checkbox checked?
        # if so, remove the element from the "Draggable Events" list
        */

        if ($("#drop-remove").is(":checked")) {
          return $(this).remove();
        }
      },
      events: [
        {
          title: "All Day Event",
          start: new Date(y, m, 1),
          className: "label label-default"
        }, {
          title: "Long Event",
          start: new Date(y, m, d - 5),
          end: new Date(y, m, d - 2),
          className: "label label-success"
        }, {
          id: 999,
          title: "Repeating Event",
          start: new Date(y, m, d - 3, 16, 0),
          allDay: false,
          className: "label label-default"
        }, {
          id: 999,
          title: "Repeating Event",
          start: new Date(y, m, d + 4, 16, 0),
          allDay: false,
          className: "label label-important"
        }, {
          title: "Meeting",
          start: new Date(y, m, d, 10, 30),
          allDay: false,
          className: "label label-info"
        }, {
          title: "Lunch",
          start: new Date(y, m, d, 12, 0),
          end: new Date(y, m, d, 14, 0),
          allDay: false,
          className: "label label-warning"
        }, {
          title: "Birthday Party",
          start: new Date(y, m, d + 1, 19, 0),
          end: new Date(y, m, d + 1, 22, 30),
          allDay: false,
          className: "label label-success"
        }, {
          title: "Click for Google",
          start: new Date(y, m, 28),
          end: new Date(y, m, 29),
          url: "http://google.com/",
          className: "label label-warning"
        }
      ]
    });
    /*
    # =============================================================================
    #   Isotope
    # =============================================================================
    */

    $container = $(".gallery-container");
    $container.isotope({});
    $(".gallery-filters a").click(function() {
      var selector;
      selector = $(this).attr("data-filter");
      $(".gallery-filters a.selected").removeClass("selected");
      $(this).addClass("selected");
      $container.isotope({
        filter: selector
      });
      return false;
    });
    /*
    # =============================================================================
    #   Popover JS
    # =============================================================================
    */

    $('#popover').popover();
    /*
    # =============================================================================
    #   Fancybox Modal
    # =============================================================================
    */

    $(".fancybox").fancybox({
      maxWidth: 700,
      height: 'auto',
      fitToView: false,
      autoSize: true,
      padding: 15,
      nextEffect: 'fade',
      prevEffect: 'fade',
      helpers: {
        title: {
          type: "outside"
        }
      }
    });
    /*
    # =============================================================================
    #   Morris Chart JS
    # =============================================================================
    */

    $(window).resize(function(e) {
      var morrisResize;
      clearTimeout(morrisResize);
      return morrisResize = setTimeout(function() {
        return buildMorris(true);
      }, 500);
    });
    $(function() {
      return buildMorris();
    });
    buildMorris = function($re) {
      var tax_data;
      if ($re) {
        $(".graph").html("");
      }
      tax_data = [
        {
          period: "2011 Q3",
          licensed: 3407,
          sorned: 660
        }, {
          period: "2011 Q2",
          licensed: 3351,
          sorned: 629
        }, {
          period: "2011 Q1",
          licensed: 3269,
          sorned: 618
        }, {
          period: "2010 Q4",
          licensed: 3246,
          sorned: 661
        }, {
          period: "2009 Q4",
          licensed: 3171,
          sorned: 676
        }, {
          period: "2008 Q4",
          licensed: 3155,
          sorned: 681
        }, {
          period: "2007 Q4",
          licensed: 3226,
          sorned: 620
        }, {
          period: "2006 Q4",
          licensed: 3245,
          sorned: null
        }, {
          period: "2005 Q4",
          licensed: 3289,
          sorned: null
        }
      ];
      if ($('#hero-graph').length) {
        Morris.Line({
          element: "hero-graph",
          data: tax_data,
          xkey: "period",
          ykeys: ["licensed", "sorned"],
          labels: ["Licensed", "Off the road"],
          lineColors: ["#5bc0de", "#60c560"]
        });
      }
      if ($('#hero-donut').length) {
        Morris.Donut({
          element: "hero-donut",
          data: [
            {
              label: "Development",
              value: 25
            }, {
              label: "Sales & Marketing",
              value: 40
            }, {
              label: "User Experience",
              value: 25
            }, {
              label: "Human Resources",
              value: 10
            }
          ],
          colors: ["#f0ad4e"],
          formatter: function(y) {
            return y + "%";
          }
        });
      }
      if ($('#hero-area').length) {
        Morris.Area({
          element: "hero-area",
          data: [
            {
              period: "2010 Q1",
              iphone: 2666,
              ipad: null,
              itouch: 2647
            }, {
              period: "2010 Q2",
              iphone: 2778,
              ipad: 2294,
              itouch: 2441
            }, {
              period: "2010 Q3",
              iphone: 4912,
              ipad: 1969,
              itouch: 2501
            }, {
              period: "2010 Q4",
              iphone: 3767,
              ipad: 3597,
              itouch: 5689
            }, {
              period: "2011 Q1",
              iphone: 6810,
              ipad: 1914,
              itouch: 2293
            }, {
              period: "2011 Q2",
              iphone: 5670,
              ipad: 4293,
              itouch: 1881
            }, {
              period: "2011 Q3",
              iphone: 4820,
              ipad: 3795,
              itouch: 1588
            }, {
              period: "2011 Q4",
              iphone: 15073,
              ipad: 5967,
              itouch: 5175
            }, {
              period: "2012 Q1",
              iphone: 10687,
              ipad: 4460,
              itouch: 2028
            }, {
              period: "2012 Q2",
              iphone: 8432,
              ipad: 5713,
              itouch: 1791
            }
          ],
          xkey: "period",
          ykeys: ["iphone", "ipad", "itouch"],
          labels: ["iPhone", "iPad", "iPod Touch"],
          hideHover: "auto",
          lineWidth: 2,
          pointSize: 4,
          lineColors: ["#a0dcee", "#f1c88e", "#a0e2a0"],
          fillOpacity: 0.5,
          smooth: true
        });
      }
      if ($('#hero-bar').length) {
        return Morris.Bar({
          element: "hero-bar",
          data: [
            {
              device: "iPhone",
              geekbench: 136
            }, {
              device: "iPhone 3G",
              geekbench: 137
            }, {
              device: "iPhone 3GS",
              geekbench: 275
            }, {
              device: "iPhone 4",
              geekbench: 380
            }, {
              device: "iPhone 4S",
              geekbench: 655
            }, {
              device: "iPhone 5",
              geekbench: 1571
            }
          ],
          xkey: "device",
          ykeys: ["geekbench"],
          labels: ["Geekbench"],
          barRatio: 0.4,
          xLabelAngle: 35,
          hideHover: "auto",
          barColors: ["#5bc0de"]
        });
      }
    };
    /*
    # =============================================================================
    #   Select2
    # =============================================================================
    */

    $('.select2able').select2();
    /*
    # =============================================================================
    #   Isotope with Masonry
    # =============================================================================
    */

    $alpha = $('#hidden-items');
    $container2 = $('#social-container');
    $(window).load(function() {
      /*
      # init isotope, then insert all items from hidden alpha
      */

      $container2.isotope({
        itemSelector: '.item'
      }).isotope('insert', $alpha.find('.item'));
      return $("#load-more").html("Load more").find("i").hide();
    });
    $('#load-more').click(function() {
      var item1, item2, item3, items, tmp;
      items = $container2.find('.social-entry');
      item1 = $(items[Math.floor(Math.random() * items.length)]).clone();
      item2 = $(items[Math.floor(Math.random() * items.length)]).clone();
      item3 = $(items[Math.floor(Math.random() * items.length)]).clone();
      tmp = $().add(item1).add(item2).add(item3);
      return $container2.isotope('insert', tmp);
    });
    /*
    # =============================================================================
    #   WYSIWYG Editor
    # =============================================================================
    */

    if ($('#summernote').length) {
      $('#summernote').summernote({
        height: 300,
        focus: true,
        toolbar: [['style', ['style']], ['style', ['bold', 'italic', 'underline', 'clear']], ['fontsize', ['fontsize']], ['color', ['color']], ['para', ['ul', 'ol', 'paragraph']], ['height', ['height']], ['insert', ['picture', 'link']], ['table', ['table']], ['fullscreen', ['fullscreen']]]
      });
    }
    /*
    # =============================================================================
    #   Typeahead
    # =============================================================================
    */

    if ($('.typeahead').length) {
      $(".states.typeahead").typeahead({
        name: "states",
        local: ["Alabama", "Alaska", "Arizona", "Arkansas", "California", "Colorado", "Connecticut", "Delaware", "Florida", "Georgia", "Hawaii", "Idaho", "Illinois", "Indiana", "Iowa", "Kansas", "Kentucky", "Louisiana", "Maine", "Maryland", "Massachusetts", "Michigan", "Minnesota", "Mississippi", "Missouri", "Montana", "Nebraska", "Nevada", "New Hampshire", "New Jersey", "New Mexico", "New York", "North Dakota", "North Carolina", "Ohio", "Oklahoma", "Oregon", "Pennsylvania", "Rhode Island", "South Carolina", "South Dakota", "Tennessee", "Texas", "Utah", "Vermont", "Virginia", "Washington", "West Virginia", "Wisconsin", "Wyoming"]
      });
      $(".countries.typeahead").typeahead({
        name: "countries",
        local: ["Andorra", "United Arab Emirates", "Afghanistan", "Antigua and Barbuda", "Anguilla", "Albania", "Armenia", "Angola", "Antarctica", "Argentina", "American Samoa", "Austria", "Australia", "Aruba", "Ã…land", "Azerbaijan", "Bosnia and Herzegovina", "Barbados", "Bangladesh", "Belgium", "Burkina Faso", "Bulgaria", "Bahrain", "Burundi", "Benin", "Saint BarthÃ©lemy", "Bermuda", "Brunei", "Bolivia", "Bonaire", "Brazil", "Bahamas", "Bhutan", "Bouvet Island", "Botswana", "Belarus", "Belize", "Canada", "Cocos [Keeling] Islands", "Congo", "Central African Republic", "Republic of the Congo", "Switzerland", "Ivory Coast", "Cook Islands", "Chile", "Cameroon", "China", "Colombia", "Costa Rica", "Cuba", "Cape Verde", "Curacao", "Christmas Island", "Cyprus", "Czechia", "Germany", "Djibouti", "Denmark", "Dominica", "Dominican Republic", "Algeria", "Ecuador", "Estonia", "Egypt", "Western Sahara", "Eritrea", "Spain", "Ethiopia", "Finland", "Fiji", "Falkland Islands", "Micronesia", "Faroe Islands", "France", "Gabon", "United Kingdom", "Grenada", "Georgia", "French Guiana", "Guernsey", "Ghana", "Gibraltar", "Greenland", "Gambia", "Guinea", "Guadeloupe", "Equatorial Guinea", "Greece", "South Georgia and the South Sandwich Islands", "Guatemala", "Guam", "Guinea-Bissau", "Guyana", "Hong Kong", "Heard Island and McDonald Islands", "Honduras", "Croatia", "Haiti", "Hungary", "Indonesia", "Ireland", "Israel", "Isle of Man", "India", "British Indian Ocean Territory", "Iraq", "Iran", "Iceland", "Italy", "Jersey", "Jamaica", "Jordan", "Japan", "Kenya", "Kyrgyzstan", "Cambodia", "Kiribati", "Comoros", "Saint Kitts and Nevis", "North Korea", "South Korea", "Kuwait", "Cayman Islands", "Kazakhstan", "Laos", "Lebanon", "Saint Lucia", "Liechtenstein", "Sri Lanka", "Liberia", "Lesotho", "Lithuania", "Luxembourg", "Latvia", "Libya", "Morocco", "Monaco", "Moldova", "Montenegro", "Saint Martin", "Madagascar", "Marshall Islands", "Macedonia", "Mali", "Myanmar [Burma]", "Mongolia", "Macao", "Northern Mariana Islands", "Martinique", "Mauritania", "Montserrat", "Malta", "Mauritius", "Maldives", "Malawi", "Mexico", "Malaysia", "Mozambique", "Namibia", "New Caledonia", "Niger", "Norfolk Island", "Nigeria", "Nicaragua", "Netherlands", "Norway", "Nepal", "Nauru", "Niue", "New Zealand", "Oman", "Panama", "Peru", "French Polynesia", "Papua New Guinea", "Philippines", "Pakistan", "Poland", "Saint Pierre and Miquelon", "Pitcairn Islands", "Puerto Rico", "Palestine", "Portugal", "Palau", "Paraguay", "Qatar", "RÃ©union", "Romania", "Serbia", "Russia", "Rwanda", "Saudi Arabia", "Solomon Islands", "Seychelles", "Sudan", "Sweden", "Singapore", "Saint Helena", "Slovenia", "Svalbard and Jan Mayen", "Slovakia", "Sierra Leone", "San Marino", "Senegal", "Somalia", "Suriname", "South Sudan", "SÃ£o TomÃ© and PrÃ­ncipe", "El Salvador", "Sint Maarten", "Syria", "Swaziland", "Turks and Caicos Islands", "Chad", "French Southern Territories", "Togo", "Thailand", "Tajikistan", "Tokelau", "East Timor", "Turkmenistan", "Tunisia", "Tonga", "Turkey", "Trinidad and Tobago", "Tuvalu", "Taiwan", "Tanzania", "Ukraine", "Uganda", "U.S. Minor Outlying Islands", "United States", "Uruguay", "Uzbekistan", "Vatican City", "Saint Vincent and the Grenadines", "Venezuela", "British Virgin Islands", "U.S. Virgin Islands", "Vietnam", "Vanuatu", "Wallis and Futuna", "Samoa", "Kosovo", "Yemen", "Mayotte", "South Africa", "Zambia", "Zimbabwe"]
      });
    }
    /*
    # =============================================================================
    #   Form Input Masks
    # =============================================================================
    */

    $(":input").inputmask();
    /*
    # =============================================================================
    #   Validation
    # =============================================================================
    */

    $("#validate-form").validate({
      rules: {
        firstname: "required",
        lastname: "required",
        username: {
          required: true,
          minlength: 2
        },
        password: {
          required: true,
          minlength: 5
        },
        confirm_password: {
          required: true,
          minlength: 5,
          equalTo: "#password"
        },
        email: {
          required: true,
          email: true
        }
      },
      messages: {
        firstname: "Please enter your first name",
        lastname: "Please enter your last name",
        username: {
          required: "Please enter a username",
          minlength: "Your username must consist of at least 2 characters"
        },
        password: {
          required: "Please provide a password",
          minlength: "Your password must be at least 5 characters long"
        },
        confirm_password: {
          required: "Please provide a password",
          minlength: "Your password must be at least 5 characters long",
          equalTo: "Please enter the same password"
        },
        email: "Please enter a valid email address"
      }
    });
    /*
    # =============================================================================
    #   Drag and drop files
    # =============================================================================
    */

    $(".single-file-drop").each(function() {
      var $dropbox;
      $dropbox = $(this);
      if (typeof window.FileReader === "undefined") {
        $("small", this).html("File API & FileReader API not supported").addClass("text-danger");
        return;
      }
      this.ondragover = function() {
        $dropbox.addClass("hover");
        return false;
      };
      this.ondragend = function() {
        $dropbox.removeClass("hover");
        return false;
      };
      return this.ondrop = function(e) {
        var file, reader;
        e.preventDefault();
        $dropbox.removeClass("hover").html("");
        file = e.dataTransfer.files[0];
        reader = new FileReader();
        reader.onload = function(event) {
          return $dropbox.append($("<img>").attr("src", event.target.result));
        };
        reader.readAsDataURL(file);
        return false;
      };
    });
    /*
    # =============================================================================
    #   File upload buttons
    # =============================================================================
    */

    $('.fileupload').fileupload();
    /*
    # =============================================================================
    #   Datepicker
    # =============================================================================
    */

    $('.datepicker').datepicker();
    nowTemp = new Date();
    now = new Date(nowTemp.getFullYear(), nowTemp.getMonth(), nowTemp.getDate(), 0, 0, 0, 0);
    checkin = $("#dpd1").datepicker({
      onRender: function(date) {
        if (date.valueOf() < now.valueOf()) {
          return "disabled";
        } else {
          return "";
        }
      }
    }).on("changeDate", function(ev) {
      var newDate;
      if (ev.date.valueOf() > checkout.date.valueOf()) {
        newDate = new Date(ev.date);
        newDate.setDate(newDate.getDate() + 1);
        checkout.setValue(newDate);
      }
      checkin.hide();
      return $("#dpd2")[0].focus();
    }).data("datepicker");
    checkout = $("#dpd2").datepicker({
      onRender: function(date) {
        if (date.valueOf() <= checkin.date.valueOf()) {
          return "disabled";
        } else {
          return "";
        }
      }
    }).on("changeDate", function(ev) {
      return checkout.hide();
    }).data("datepicker");
    /*
    # =============================================================================
    #   Daterange Picker
    # =============================================================================
    */

    $(".date-range").daterangepicker({
      format: "MM/dd/yyyy",
      separator: " to ",
      startDate: Date.today().add({
        days: -29
      }),
      endDate: Date.today(),
      minDate: "01/01/2012",
      maxDate: "12/31/2014"
    });
    /*
    # =============================================================================
    #   Timepicker
    # =============================================================================
    */

    $("#timepicker-default").timepicker();
    $("#timepicker-24h").timepicker({
      minuteStep: 1,
      showSeconds: true,
      showMeridian: false
    });
    $("#timepicker-noTemplate").timepicker({
      template: false,
      showInputs: false,
      minuteStep: 5
    });
    $("#timepicker-modal").timepicker({
      minuteStep: 1,
      secondStep: 5,
      showInputs: false,
      modalBackdrop: true,
      showSeconds: true,
      showMeridian: false
    });
    $("#cp1").colorpicker({
      format: "hex"
    });
    $("#cp2").colorpicker();
    $("#cp3").colorpicker();
    /*
    # =============================================================================
    #   Skycons
    # =============================================================================
    */

    $('.skycons-element').each(function() {
      var canvasId, skycons, weatherSetting;
      skycons = new Skycons({
        color: "white"
      });
      canvasId = $(this).attr('id');
      weatherSetting = $(this).data('skycons');
      skycons.add(canvasId, Skycons[weatherSetting]);
      return skycons.play();
    });
    /*
    # =============================================================================
    #   Login/signup animation
    # =============================================================================
    */

    $(window).load(function() {
      return $(".login-container").addClass("active");
    });
    /*
    # =============================================================================
    #   FitVids
    # =============================================================================
    */

    $(".timeline-content").fitVids();
    /*
    # =============================================================================
    #   Timeline animation
    # =============================================================================
    */

    timelineAnimate = function(elem) {
      return $(".timeline.animated li").each(function(i) {
        var bottom_of_object, bottom_of_window;
        bottom_of_object = $(this).position().top + $(this).outerHeight();
        bottom_of_window = $(window).scrollTop() + $(window).height();
        if (bottom_of_window > bottom_of_object) {
          return $(this).addClass("active");
        }
      });
    };
    timelineAnimate();
    $(window).scroll(function() {
      return timelineAnimate();
    });
    /*
    # =============================================================================
    #   Input placeholder fix
    # =============================================================================
    */

    if (!Modernizr.input.placeholder) {
      $("[placeholder]").focus(function() {
        var input;
        input = $(this);
        if (input.val() === input.attr("placeholder")) {
          input.val("");
          return input.removeClass("placeholder");
        }
      }).blur(function() {
        var input;
        input = $(this);
        if (input.val() === "" || input.val() === input.attr("placeholder")) {
          input.addClass("placeholder");
          return input.val(input.attr("placeholder"));
        }
      }).blur();
      return $("[placeholder]").parents("form").submit(function() {
        return $(this).find("[placeholder]").each(function() {
          var input;
          input = $(this);
          if (input.val() === input.attr("placeholder")) {
            return input.val("");
          }
        });
      });
    }
  });

}).call(this);
