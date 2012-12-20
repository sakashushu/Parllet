/****************************************************************
* 機　能： 入力された値が日付でYYYY/MM/DD形式になっているか調べる
* 引　数： datestr　入力された値
* 戻り値： 正：true　不正：false
****************************************************************/
function ckDate(datestr) {
    // 正規表現による書式チェック
    if(!datestr.match(/^\d{4}\/\d{2}\/\d{2}$/)){
        return false;
    }
    var vYear = datestr.substr(0, 4) - 0;
    var vMonth = datestr.substr(5, 2) - 1; // Javascriptは、0-11で表現
    var vDay = datestr.substr(8, 2) - 0;
    // 月,日の妥当性チェック
    if(vMonth >= 0 && vMonth <= 11 && vDay >= 1 && vDay <= 31){
        var vDt = new Date(vYear, vMonth, vDay);
        if(isNaN(vDt)){
            return false;
        }else if(vDt.getFullYear() == vYear && vDt.getMonth() == vMonth && vDt.getDate() == vDay){
            return true;
        }else{
            return false;
        }
    }else{
        return false;
    }
}
