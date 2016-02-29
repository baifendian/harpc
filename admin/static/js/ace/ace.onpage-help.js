jQuery(function($) {
	var help = null;
	$(window).on('hashchange.start_help', function(e) {
		if(help == null && window.location.hash == '#help') {
			help = new ace.Onpage_Help($)
			help.init()
			help.disable();
			
			//add #help tag to links to enable help
			$(document).on('click.start_help', '.sidebar .nav-list a', function() {
				var href = $(this).attr('href');
				if( !href.match(/\#help$/) ) $(this).attr('href', href+'#help');
			});
		}
	}).triggerHandler('hashchange.start_help');
	
	
	//some buttons inside demo pages to launch help
	$(document).on(ace.click_event, '.btn-display-help', function(e) {
		e.preventDefault();

		if(help == null) {
			help = new ace.Onpage_Help($)
			help.init()
			help.disable();
			
			//
			$('#ace-toggle-onpage-help').trigger('click');
		}
		
		var section = $(this).attr('href');
		help.show_help(section);
	});
});

ace.Onpage_Help = function($) {
	if( !window.Node ) window.Node = {
		ELEMENT_NODE: 1,
		ATTRIBUTE_NODE: 2,
		TEXT_NODE: 3,
		COMMENT_NODE: 8
	};
	
	var $base = ace.vars['base'] || '../..';

	var section_start = {};
	var section_end = {};
	var rects = {};
	
	var created = false;
	var active = false;
	
	var self = this;
	var settings = {}
	var ovfx = '';
	var container = null;
	
	var body_h, body_w;
	
	var captureFocus = function() {
		if(!container) return;
		var scroll = -1;
		//like bootstrap modal
		$(document)
		.off('focusin.ace.help') //remove any previously attached handler
		.on('focusin.ace.help', function (e) {
			if (!( container[0] == e.target || $.contains(container[0], e.target) )) {
			  container.focus();
			}

			if(e.target == document && scroll > -1) {
				//when window regains focus and container is focused, it scrolls to bottom
				//so we put it back to its place
				$('body,html').scrollTop(scroll);
				scroll = -1;
			}
		})

		$(window).on('blur.ace.help', function(){
			scroll = $(window).scrollTop();
		});
	}
	var releaseFocus = function() {
		$(document).off('focusin.ace.help');
		$(window).off('blur.ace.help');
	}

	this.enable = function() {
		if(active) return;

		active = true;
		settings['navbar'] = ace.settings.is('navbar', 'fixed')
		settings['sidebar'] = ace.settings.is('sidebar', 'fixed')
		settings['breadcrumbs'] = ace.settings.is('breadcrumbs', 'fixed')
		
		ace.settings.navbar_fixed(false , false);//disable fixed navbar, sidebar, etc
		
		if( !created ) {
			this.init();
			created = true;
		}
		$('.ace-onpage-help-backdrop, .ace-onpage-help').removeClass('hidden');
		
		ovfx = document.body.style.overflowX;
		document.body.style.overflowX = 'hidden';//hide body:overflow-x
		$('#btn-scroll-up').css('z-index', 1000000);
		
		$(window).triggerHandler('resize.onpage_help');
		captureFocus();
	}
	
	this.disable = function() {
		
		active = false;
		$('.ace-onpage-help-backdrop, .ace-onpage-help').addClass('hidden');

		document.body.style.overflowX = ovfx;//restore body:overflow-x
		$('#btn-scroll-up').css('z-index', '');

		//restore fixed state of navbar, sidebar, etc
		if( settings['breadcrumbs'] ) {
			ace.settings.breadcrumbs_fixed(true, false, false);
		}
		if( settings['sidebar'] ) {
			ace.settings.sidebar_fixed(true, false, false);
		}
		if( settings['navbar'] ) {
			ace.settings.navbar_fixed(true, false, false);
		}
		
		releaseFocus();
	}
	
	this.is_active = function() {
		return active;
	}
	this.show_help = function(section) {
		launch_help_modal(section, true);
	}
	
	
	this.init = function() {
		container = $('<div class="ace-onpage-help-container" tabindex="-1" />').appendTo('body');
		
		container
		.append('<div class="ace-onpage-help-backdrop" />')
		.append('\<div class="ace-settings-container ace-help-container">\
	<div id="ace-toggle-onpage-help" class="btn btn-app btn-xs btn-info ace-settings-btn ace-toggle-onpage-help">\
		<i class="ace-toggle-help-text ace-icon fa fa-question bigger-150"></i>\
	</div>\
</div>');

		$(document).on('settings.ace.help', function(ev, event_name, event_val) {
		   if(event_name == 'main_container_fixed') {
			 if(event_val) container.addClass('container');
			 else container.removeClass('container');
		   }
		}).triggerHandler('settings.ace.help', ['main_container_fixed', $('.main-container').hasClass('container')])
		

		$('#ace-toggle-onpage-help').on('click', function(e) {
			if(active) {
				self.disable();
			}
			else {
				self.enable();
			}
			$(this).find('.ace-toggle-help-text').removeClass('ace-toggle-help-text');
			$(this).toggleClass('btn-grey btn-info').parent().toggleClass('active');
			e.preventDefault();
		})

		//find all comments
		var comments = $('*').contents().filter(function(){ return this.nodeType == Node.COMMENT_NODE; })
		$(comments).each(function() {
			var match
			if( (match = $.trim(this.data).match(/#section\s*:\s*([\w\-\.\/]+)/i)) ) {
				var section_name = match[1];
				if( !(section_name in section_start) ) section_start[ section_name ] = this;
			}
			if( (match = $.trim(this.data).match(/\/section\s*:\s*([\w\-\.\/]+)/i)) ) {
				var section_name = match[1];
				if( !(section_name in section_end) ) section_end[ section_name ] = this;
			}
		})


		//update to correct position and size
		$(window).on('resize.onpage_help', function() {
			if(!active) return;
			body_h = document.body.scrollHeight - 2;
			body_w = document.body.scrollWidth - 2;

			//we first calculate all positions
			//because if we calculate one position and then change DOM,
			//next position calculation will become slow on Webkit, because it tries to re-calculate things
			//i.e. batch call all and save offsets and scrollWidth, etc and then use them later in highlight_section
			//Firefox doesn't have such issue
			for(var name in section_start) {
				if(section_start.hasOwnProperty(name)) {
					save_section_position(name);
				}
			}
			for(var name in section_start) {
				if(section_start.hasOwnProperty(name)) {
					highlight_section(name);
				}
			}

		})

		//$('.alert').on('closed.bs.alert', function() {
			//$(window).triggerHandler('resize.onpage_help');
		//});

		created = true;
	}


	function save_section_position(name) {
		if( !(name in section_start) || !(name in section_end) ) return;
		
		var node = section_start[name];
		var start = $(node).next().eq(0);
		var end = $(section_end[name]).prev().eq(0);
		
		var start_hidden = start.is(':hidden');
		var end_hidden = end.is(':hidden');
		if( start_hidden && end_hidden ) {
			rects[name] = {is_hidden: true}
			return;
		}
		
		if(start_hidden) start = end;
		else if(end_hidden) end = start;
		
		//get the start and end position of our rectangle to be drawn!
		var off1 = start.offset();
		var off2 = end.offset();
		if( !off1 || !off2 ) {
			rects[name] = {is_hidden: true}
			return;
		}
		

		var x1, y1, x2, y2, w2, h2;
		if(off1.left < off2.left) {
			x1 = parseInt(off1.left);
			x2 = parseInt(off2.left);
			w2 = parseInt(end.outerWidth());
		} else {
			x1 = parseInt(off2.left);
			x2 = parseInt(off1.left);
			w2 = parseInt(start.outerWidth());
		}
		
		if(off1.top < off2.top) {
			y1 = parseInt(off1.top);
			y2 = parseInt(off2.top);
			h2 = parseInt(end.outerHeight());
		} else {
			y1 = parseInt(off2.top);
			y2 = parseInt(off1.top);
			h2 = parseInt(start.outerHeight());
		}
		
		x1 -= 1;
		y1 -= 1;
		x2 += 1;
		y2 += 1;
		
		var width = x2 + w2 - x1, height = y2 + h2 - y1;
		
		//if out of window rect
		if(x1 + width < 2 || x1 > body_w || y1 + height < 2 || y1 > body_h ) {
			rects[name] = {is_hidden: true}
			return;
		}
		
		rects[name] = {
			left: x1,
			top: y1,
			width: width,
			height: height			
		}
	}

	function highlight_section(name) {
		if( !(name in rects) || !container ) return;

		var div = container.find('.ace-onpage-help[data-section="'+name+'"]').eq(0);
		if(div.length == 0)	{
			div = $('<a class="ace-onpage-help" href="#" />').appendTo(container);
			div.attr('data-section', name);
			
			div.on(ace.click_event, function(e) {
				e.preventDefault();
				launch_help_modal(name);
			});
		}

		var rect = rects[name];
		if(rect.is_hidden) {
			div.addClass('hidden');
			return;
		}
		
		div.css({
			left: rect.left,
			top: rect.top,
			width: rect.width,
			height: rect.height
		});
		

		div.removeClass('hidden');
		div.removeClass('smaller smallest');
		if(rect.height < 55 || rect.width < 55) {
			div.addClass('smallest');
		}
		else if(rect.height < 75 || rect.width < 75) {
			div.addClass('smaller');
		}

	}


	var nav_list = [];
	var nav_pos = -1;
	var mbody = null, mbody_scroll = null;

	function launch_help_modal(name, save_to_list) {
	    name = name.replace(/^#/g, '');
	  
		var modal = $('#onpage-help-modal');
		if(modal.length == 0) {
			modal = $('<div id="onpage-help-modal" class="modal onpage-help-modal" tabindex="-1" role="dialog" aria-labelledby="HelpModalDialog" aria-hidden="true">\
			  <div class="modal-dialog modal-lg">\
				<div class="modal-content">\
					<div class="modal-header">\
					  <div class="pull-right modal-buttons">\
						<button aria-hidden="true" data-goup="modal" type="button" class="disabled btn btn-white btn-success btn-sm"><i class="ace-icon fa fa-level-up fa-flip-horizontal bigger-125 icon-only"></i></button>\
						&nbsp;\
						<button aria-hidden="true" data-goback="modal" type="button" class="disabled btn btn-white btn-info btn-sm"><i class="ace-icon fa fa-arrow-left icon-only"></i></button>\
						<button aria-hidden="true" data-goforward="modal" type="button" class="disabled btn btn-white btn-info  btn-sm"><i class="ace-icon fa fa-arrow-right icon-only"></i></button>\
						&nbsp;\
						<button aria-hidden="true" data-dismiss="modal" class="btn btn-white btn-danger btn-sm" type="button"><i class="ace-icon fa fa-times icon-only"></i></button>\
					  </div>\
					  <h4 class="modal-title">Help Dialog/ <small></small></h4>\
					</div>\
					<div class="modal-body"> <div class="onpage-help-content"></div> </div>\
				</div>\
			  </div>\
			</div>').appendTo('body');
		
			mbody = modal.find('.modal-body');
			modal.css({'overflow' : 'hidden'})
			
			mbody.ace_scroll({hoverReset: false, size: $(window).innerHeight() - 150, lockAnyway: true, styleClass: 'scroll-margin scroll-dark'})
			
			$('#onpage-help-modal')
			.on('show.bs.modal', function() {
				releaseFocus();
			})
			.on('hidden.bs.modal', function() {
				captureFocus();
			});

			$(document).on('shown.ace.widget hidden.ace.widget', '.help-content .widget-box', function() {
				mbody.ace_scroll('reset');
			});
		}
		


		if( !modal.hasClass('in') ) {
			if(document.body.lastChild != modal.get(0)) $(document.body).append(modal);//move it to become the last element of body
			modal.modal('show');
			var diff = parseInt(modal.find('.modal-dialog').css('margin-top'));
			diff = diff + 110 + parseInt(diff / 2);
			mbody.ace_scroll('update', { size: $(window).innerHeight() - diff });
		}
	
		modal.find('.modal-title').wrapInner("<span class='hidden' />").append('<i class="fa fa-spinner fa-spin blue bigger-125"></i>');
		var content = $('.onpage-help-content');
		content.addClass('hidden')
		
		$(document.body).removeClass('modal-open');
		if(name.indexOf('file:') >= 0) {
			var parts = name.match(/file\:(.*)\:(.+)/i);
			if(parts.length == 3) display_codeview(parts[2], parts[1], false);
			return;
		}
		
		
		

		var url = name.replace(/\..*$/g, '')
		var parts = url.split('/');
		if(parts.length == 1) {
			if(url.length == 0) url = 'intro';
			url = url+'/index.html';
		}
		else if(parts.length > 1) {
			url = url+'.html';
		}
		
		
		$.ajax({url: $base+"/docs/sections/" + url, dataType: 'text'})
		.done(function(result) {
			var find1 = 'data-id="#'+name+'"';
			var pos1 = result.indexOf(find1);
			
			var tname = name;
			if(pos1 == -1) {
				//if no data-id="#something.part" go for data-id="#something" instead
				var tpos
				var tfind1 
				if((tpos = tname.lastIndexOf('.')) > -1) {
					tname = tname.substr(0, tpos);
					tfind1 = 'data-id="#'+tname+'"';
					pos1 = result.indexOf(tfind1);
					pos1 += tfind1.length + 1
				}
			}
			else pos1 += find1.length + 1

			
			var pos2 = result.indexOf("</h", pos1);
			modal.find('.modal-title').addClass('blue').html( result.substring(pos1, pos2) );


			find1 = '<!-- #section:'+name+' -->';
			pos1 = result.indexOf(find1);
			pos2 = result.indexOf('<!-- /section:'+name+' -->', pos1);


			result = result.substring(pos1 + find1.length + 1, pos2);
			result = result.replace(/\<pre(?:\s+)data\-language=["'](?:html|javascript|php)["']\>([\S|\s]+?)\<\/pre\>/ig, function(a, b){
				return a.replace(b , b.replace(/\</g , '&lt;').replace(/\>/g , '&gt;'));
			});
			content.empty().append(result);
			
			content
			.find('.info-section').each(function() {
				var header = $(this).prevAll('.info-title').eq(0);
				if(header.length == 0) return false;
				
				header = header.addClass('widget-title').wrap('<div class="widget-header" />')
				.parent().append('<div class="widget-toolbar no-border">\
				<a href="#" data-action="collapse">\
					<i data-icon-hide="fa-minus" data-icon-show="fa-plus" class="ace-icon fa fa-plus"></i>\
				</a>\
			</div>').closest('.widget-header');

				$(this).wrap('<div class="widget-box transparent collapsed"><div class="widget-body"><div class="widget-main"></div></div></div>');
				$(this).closest('.widget-box').prepend(header);
			});

			content.removeClass('hidden');

			content.find('span.thumbnail img').each(function() {
				var src = $(this).attr('src');
				$(this)
				.attr('src', $base+"/docs/" + src)
				.one('load', function() {
					mbody.ace_scroll('reset');
				});
			});

			Rainbow.color(content.get(0), function(){
				mbody.ace_scroll('reset');
			});

			//save history list
			add_to_nav_list(name, save_to_list);
		
			var pos = -1;
			if((pos = name.lastIndexOf('.')) > -1) {
				name = name.substr(0, pos);
				modal.find('button[data-goup=modal]').removeClass('disabled').attr('data-url', name);
			}
			else {
				modal.find('button[data-goup=modal]').addClass('disabled').blur();
			}
		
		})
		.fail(function() {
			modal.find('.modal-title').find('.fa-spin').remove().end().find('.hidden').children().unwrap();
			mbody.ace_scroll('reset');
		});
	}//launch_help_modal

	$(document).on(ace.click_event, '.help-content > .widget-box > .widget-header > .info-title', function(e) {
		var widget_box = $(this).closest('.widget-box').widget_box('toggle');
	});
	$(document).on(ace.click_event, '.help-more', function(e) {
		e.preventDefault();
		var href = $(this).attr('href');
		launch_help_modal(href);
	});
	
	
	function add_to_nav_list(name, save_to_list) {
		if(save_to_list !== false) {
			if(nav_list.length > 0) {
				nav_list = nav_list.slice(0, nav_pos + 1);
			}
			if(nav_list[nav_list.length - 1] != name) {
				nav_list.push(name);
				nav_pos = nav_list.length - 1;
			}
		}
		
		var modal = $('#onpage-help-modal');
		if(nav_pos == 0){
			modal.find('button[data-goback=modal]').addClass('disabled').blur();
		}
		else {
			modal.find('button[data-goback=modal]').removeClass('disabled');
		}
		
		if(nav_pos == nav_list.length - 1){
			modal.find('button[data-goforward=modal]').addClass('disabled').blur();
		}
		else {
			modal.find('button[data-goforward=modal]').removeClass('disabled');
		}
	}
	
	$(document).on(ace.click_event, 'button[data-goforward=modal]', function() {
		if(nav_pos < nav_list.length - 1) {
			nav_pos++;
			launch_help_modal(nav_list[nav_pos], false);
		}
	});
	$(document).on(ace.click_event, 'button[data-goback=modal]', function() {
		if(nav_pos > 0) {
			nav_pos--;
			launch_help_modal(nav_list[nav_pos], false);
		}
	});
	$(document).on(ace.click_event, 'button[data-goup=modal]', function() {
		var $this = $(this), url;
		if( $this.hasClass('disabled') || !(url = $this.attr('data-url')) ) return;
		
		launch_help_modal(url , true)
	});
	
	
	$(document).on(ace.click_event, '.open-file[data-open-file]', function() {
		$('#onpage-help-modal').find('.modal-title').wrapInner("<span class='hidden' />").append('<i class="fa fa-spinner fa-spin blue bigger-125"></i>');
		$('.onpage-help-content').addClass('hidden')
	
		var url = $(this).text();
		var language = $(this).attr('data-open-file');
		display_codeview(url, language, true);
	});
	
	
	function display_codeview(url, language, save_to_list) {
		$.ajax({url: $base+'/'+url, dataType:'text'})
		.done(function(content) {
			if(language != 'json') {
				if(language != 'css') {
					//replace each tab character with two spaces (only those that start at a new line)
					content = content.replace(/\n[\t]{1,}/g, function(p, q) {
						return p.replace(/\t/g, "  ");
					});
				} else {
					content = content.replace(/\t/g , "  ")
				}
			}
			else {
				language = '';
				content = JSON.stringify(JSON.parse(content), null, 2);
			}

			var modal = $('#onpage-help-modal');
			add_to_nav_list('file:'+language+':'+url, save_to_list);
			modal.find('button[data-goup=modal]').addClass('disabled').blur();

			content = content.replace(/\>/g, '&gt;').replace(/\</g, '&lt;')
			Rainbow.color(content, language, function(highlighted_code) {
				modal.find('.modal-title').html(url).wrapInner('<code />');;
				$('.onpage-help-content').removeClass('hidden').empty().html(highlighted_code).wrapInner('<pre data-language="'+language+'" />');
				modal.find('.modal-body').ace_scroll('reset');
			});
		});
	}

}