/*
 * Parllet共通のjavascript
 */

/**
 * 繰上げ関数
 * @param num
 * @param digit
 * @returns {Number}
 */
function roundup(num,digit){
	var d=Math.pow(10,(digit));
	var n=Math.ceil(num*d)/d;
	return n;
}
