/**
 * borderMenu.js v1.0.0
 * http://www.codrops.com
 *
 * Licensed under the MIT license.
 * http://www.opensource.org/licenses/mit-license.php
 * 
 * Copyright 2013, Codrops
 * http://www.codrops.com
 * 
 * 2013/11/18 shuichi sakamoto 上記を元にしてjQuery化等カスタマイズ
 */
;(function() {
	function init() {
//		var eventtype = mobilecheck() ? 'touchstart click' : 'click',
		var eventtype = 'click',
			resetMenu = function() {
				jQuery('#bt-menu').removeClass('bt-menu-open');
				jQuery('#bt-menu').addClass('bt-menu-close');
				if (typeof jQuery('.large_cate').attr("id") != 'undefined') jQuery('.large_cate').removeAttr('id');
				if (typeof jQuery('.small_cate').attr("id") != 'undefined') jQuery('.small_cate').removeAttr('id');
			};
		jQuery('#aLarge_cate, #aSmall_cate').on(eventtype, function() {
			var strCate = this.id==='aLarge_cate' ? '.large_cate' : '.small_cate';
			jQuery(strCate).attr('id', 'bt-menu');
			jQuery('#bt-menu').children('a.bt-menu-trigger').click();
			return false;
		});
		jQuery(document).on(eventtype, '#bt-menu a.bt-menu-trigger', function(ev) {
			ev.stopPropagation();
			ev.preventDefault();
			
			if (jQuery('#bt-menu').hasClass('bt-menu-open')) {
				resetMenu();
			} else {
				jQuery('#bt-menu').removeClass('bt-menu-close');
				jQuery('#bt-menu').addClass('bt-menu-open');
			}
		});
		jQuery(document).on(eventtype, '#bt-menu div.bt-overlay', function(ev) {
			ev.stopPropagation();
			ev.preventDefault();
			resetMenu();
		});
		jQuery(document).on('click', '#bt-menu ul a', function(ev) { 
			if (jQuery('#bt-menu').hasClass('bt-menu-open')) resetMenu();
		});
		
		// Android 2.3 でスクロールしないバグへの対応
		if (navigator.userAgent.indexOf('Android') > 0) {
			var touchStartPositionX,
				touchStartPositionY,
				touchMovePositionX,
				touchMovePositionY,
				moveFarX,
				moveFarY,
				startScrollX,
				startScrollY,
				moveScrollX,
				moveScrollY;
		
			jQuery(document).on('touchstart', '#bt-menu .divUlFrm', function(e) {
				var touch = e.originalEvent.touches[0];
				touchStartPositionX = touch.pageX;
				touchStartPositionY = touch.pageY;
				//タッチ前スクロールをとる
				startScrollX = jQuery(this).scrollLeft();
				startScrollY = jQuery(this).scrollTop();
			});
			jQuery(document).on('touchmove', '#bt-menu .divUlFrm', function(e) {
				var touch = e.originalEvent.touches[0];
				e.preventDefault();
				//現在の座標を取得
				touchMovePositionX = touch.pageX;
				touchMovePositionY = touch.pageY;
				//差をとる
				moveFarX = touchStartPositionX - touchMovePositionX;
				moveFarY = touchStartPositionY - touchMovePositionY;
				//スクロールを動かす
				moveScrollX = startScrollX +moveFarX;
				moveScrollY = startScrollY +moveFarY;
				jQuery(this).scrollLeft(moveScrollX);
				jQuery(this).scrollTop(moveScrollY);
			});
		}
	}
	
	init();
	
})();
