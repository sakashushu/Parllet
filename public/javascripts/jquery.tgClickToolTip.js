/* 
 * for jQuery 1.8.1
 * jquery.tgClickToolTip
 * 
 * 【概要】
 * <a>タグで設定されているリンククリックで、
 * 同名IDで設定されている<p>タグ内容をツールチップとして表示します。
 * 
 * 【リンク設定例】[?]部分です。
 * <a href="#note1" class="clickToolTip">?</a>
 * 
 * 【ツールチップ内容設定例】「invisible」classは最初から設定しておいてください。
 * <p id="note1" class="toolTip invisible">ツールチップ内容<br />ツールチップ内容</p>
 * 
 * 
 * @Copyright : 2012 toogie | http://wataame.sumomo.ne.jp/archives/1719
 * @Version   : 1.1
 * @Modified  : 2012-09-19
 * @Modified(sakashushu)  : 2013-07-30
 * 
 */

;(function(jQuery){
	
	jQuery.fn.tgClickToolTip = function(options){
		
		var opts = jQuery.extend({}, jQuery.fn.tgClickToolTip.defaults, options),
			onMenu = 'out';
		
		//ツールチップ上にポインタがあれば「over」、無ければ「out」
		jQuery(opts.selector).each(function() {
			// リンクの #note** を取得
			var targetNote = jQuery(this).attr('href');
			
			jQuery('p'+targetNote).hover(function() {
				onMenu = 'over';
				
			},function() {
				onMenu = 'out';
				
			});
		});
		
		//セレクタのEnter時はクリックイベントへ
		jQuery(opts.selector).keypress(function(e) {
			if(e.which==13) {
				e.preventDefault();
				jQuery(this).click();
			}
		});
		
		//セレクタのクリックイベント
		jQuery(opts.selector).click(function(){
			
			// リンクの #note** を取得
			var targetNote = jQuery(this).attr('href');
			
			// [?]の座標を取得
			var position = jQuery(this).position();
			var intPositionTop = parseInt(opts.PositionTop);		/* 数値型に変換 */
			var intPositionLeft = parseInt(opts.PositionLeft);
			var newPositionTop  = position.top + intPositionTop;	/* + 数値で下方向へ移動 */
			var newPositionLeft = position.left + intPositionLeft;	/* + 数値で右方向へ移動 */
			
			// ツールチップの位置を調整
			jQuery('p'+targetNote).css({'top': newPositionTop + 'px', 'left': newPositionLeft + 'px'});
			
			// ツールチップの class="invisible" を削除
			jQuery('p'+targetNote).removeClass('invisible');
			
			return false;	// アドレスバーに「URL#・・・」と表示されないようにする
		});
		
		// 表示されたツールチップを隠す処理（「out」の時はマウスクリックで隠す）
		jQuery('html').mousedown(function(){
			if (onMenu==='out')
				jQuery('p.toolTip').addClass('invisible');
		});
	}
	
	// default option
	jQuery.fn.tgClickToolTip.defaults = {
		selector : 'a.clickToolTip',
		PositionTop : '-10',
		PositionLeft : '40',
	};
	
})(jQuery);
