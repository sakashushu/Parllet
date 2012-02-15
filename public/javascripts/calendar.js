/***************************************************************************************************
 * デザイン設定／必要に応じて修正してください。
 ***************************************************************************************************/

var wlock   = 1;             // 常に全面表示の場合は 1、そうでない場合は 0
var clear_f = 0;             // 1:クリアボタン表示、0:非表示
var c_body  = '#ddddff';     // ボディの背景
var c_table = '#dddddd';     // カレンダーの背景
var c_month = '#003399';     // 月の背景
var f_month = '#ffffff';     // 月の文字
var c_week  = '#a6c2ff';     // 週の背景
var f_week  = '#000000';     // 週の文字
var c_date  = '#ffffff';     // 日の背景
var c_datex = '#eeeeee';     // 日の背景(先月・来月部分)
var c_dateo = '#a6c2ff';     // 日の背景(本日部分)
var f_date  = '#000000';     // 日の文字
var s_date  = '#ffccff';     // 休祝日の背景
var t_date  = '#ccffcc';     // 土曜日の背景

/***************************************************************************************************
 * 設定はここまで
 ***************************************************************************************************/

/***************************************************************************************************
 *                     カレンダーによる日付入力スクリプト calendar.js
 * =================================================================================================
 *                                 The original version
 *                  Copyright(c)1999 Toshirou Takahashi tato@fureai.or.jp
 *                 Support http://www.fureai.or.jp/~tato/JS/BOOK/INDEX.HTM
 * =================================================================================================
 *
 * 【更新記録】
 * 2002. 2.22 (再)「＞＞」クリックした時に当日の日が翌月に存在しない場合に翌月カレンダーがおかしくなる(Thanx TEC関さん)
 * 2002. 2.21 「＞＞」クリックした時に当日の日が翌月に存在しない場合に翌月カレンダーがおかしくなる(Thanx TEC関さん)
 * 2002. 2.21 Moz用サイズ微調整
 * 2002. 1.28 1月時の前月と12月時の来月グレー日付をクリックで当年の出力になっていたのを修正(Thanx 吉野理希さん)
 * 2001.12.28 今月へ戻る「O」ボタンでエラーが出ていたのを修正 ( for WinIE6.0 )
 *
 * =================================================================================================
 *                                The reconstruction version
 *                         Created by Yoshio Kanaya on April 2, 2004
 *                                 http://www.kanaya440.com/
 * =================================================================================================
 *
 * 【更新記録】
 * 2008. 6. 8 ブラウザによって年が正しく表示されない点を修正（ for Firefox ）
 * 2007. 4. 5 2007年祝日法改正に対応
 * 2005. 7.30 クリアボタンの表示、非表示を指定できるようにする
 * 2005. 7.19 クリア機能の追加（佐々木様による機能追加）
 * 2005. 7.19 年を過去にしていくと1898年以降がおかしくなるバグを修正（佐々木様によるご指摘、修正）
 * 2004. 6.22 Firefoxに対応するようにする
 *
 * Toshirou Takahashi氏作成のスクリプトを改造
 *【改造内容】
 * ・Firefoxに対応（サイズ調整）
 * ・NSの場合もクリックしたそばにウィンドウ表示されるようにする
 * ・スクリーンの上にでる場合は下に、スクリーン右に出る場合は左に表示されるようにする
 * ・カラーのカスタマイズを簡易（変数化）にする
 * ・月移動だけでなく、年移動もできるようにする
 * ・土日祝祭日の背景色を変えるようにする      ┐春分の日・秋分の日・国民に休日にも対応
 * ・マウスオーバーで祝日名を表示するようにする┘
 * ・戻り値の年月日の書式指定を可能にする
 *   日付タイプ : 'g'    → 年号の頭文字を返します (M、T、S、H)
 *                'gg'   → 年号の先頭の 1 文字を漢字で返します (明、大、昭、平)
 *                'ggg'  → 年号を返します (明治、大正、昭和、平成)
 *                'yy'   → 西暦の年を下 2 桁の数値で返します (00 ～ 99)
 *                'yyyy' → 西暦の年を 4 桁の数値で返します (100 ～ 9999)
 *                'm'    → 月を表す数値を返します。1 桁の場合、先頭に 0 が付きません (1 ～ 12)
 *                'mm'   → 月を表す数値を返します。1 桁の場合、先頭に 0 が付きます (01 ～ 12)
 *                'd'    → 日付を返します。1 桁の場合、先頭に 0 が付きません (1 ～ 31)
 *                'dd'   → 日付を返します。1 桁の場合、先頭に 0 が付きます (01 ～ 31)
 *                'w'    → 曜日を英語 (省略形) で返します (Sun ～ Sat)
 *                'ww    → 曜日を英語で返します (Sunday ～ Saturday)
 *                'a     → 曜日を日本語 (省略形) で返します (日～土)
 *                'aa    → 曜日を日本語で返します (日曜日～土曜日)
 *
 *   ※デフォルトは、'yyyy/mm/dd'
 *
 * =================================================================================================
 *
 *  Syntax  : wrtCalendar( event,formElementObject[,formFlg][,moveMonthFlg][,winOpenFlg] )
 *
 *  Example : 受付日:<input type="text" name="e1"><input type="button"
 *                          name="Calendar" value="Calendar"
 *                          onClick="wrtCalendar(event,this.form.e1)">
 *
 *      例1 : wrtCalendar(event,this.form.e1)
 *      例2 : wrtCalendar(event,this.form.e1,'yyyy/mm/dd')
 *      例3 : wrtCalendar(event,this.form.e1,'yyyy年m月d日(ww)')
 *      例4 : wrtCalendar(event,this.form.e1,'gg年m月d日(aa)')
 *      例5 : wrtCalendar(event,this.form.e1,'m/d')
 *      例6 : wrtCalendar(event,this.form.e1,'mm/dd')
 *
 ***************************************************************************************************/

var now    = new Date();
var absnow = now;
var Win=navigator.userAgent.indexOf('Win')!=-1;
var Mac=navigator.userAgent.indexOf('Mac')!=-1;
var X11=navigator.userAgent.indexOf('X11')!=-1;
var Moz=navigator.userAgent.indexOf('Gecko')!=-1;
var Fir=navigator.userAgent.indexOf('Firefox')!=-1;
var Opera=!!window.opera;
var winflg=1;

function wrtCalendar(e,oj,flg,arg1,arg2){

  if(Opera)return;
  oj.blur();

  if(!arguments[2]) flg  = 'yyyy/mm/dd';
  if(!arguments[3]) arg1 = 0;
  if(!Moz)
  if(arguments[4]||arguments[4]==0) winflg = 0;

  //-初期化
  if(arg1==0)now = new Date();

  //-年月日取得
  nowdate  = now.getDate();
  nowmonth = now.getMonth();
  nowyear  = now.getFullYear();

  //-月移動処理
  if(arg1 == 12){                        //arg1が12なら
    nowyear++;                                //1年加算
  } else if(arg1 == -12){                 //arg1が-12なら
    nowyear--;                                //1年減算
  } else if(nowmonth == 11 && arg1 > 0){ //12月でarg1が+なら
    nowmonth = -1 + arg1; nowyear++;          //月はarg1-1;1年加算
  } else if(nowmonth == 0 && arg1 < 0){  //1月でarg1が-なら
    nowmonth = 12 + arg1; nowyear--;          //月はarg1+12;1年減算
  } else {                               //2-11月なら
    nowmonth +=  arg1;                        //月は+arg1
  }

  //-2000年問題対応
  if(nowyear < 1900) nowyear = 1900 + nowyear;

  //-現在月を確定
  now = new Date(nowyear,nowmonth,1);

  //-YYYYMM作成
  nowyyyymm = nowyear * 100 + nowmonth;

  //-YYYY/MM作成
  nowtitlemonth = nowmonth + 1;
  if(nowtitlemonth < 10) nowtitlemonth = '0' + nowtitlemonth
  nowtitleyyyymm = nowyear + ' / ' + nowtitlemonth;

  //-週設定
  week = new Array('日','月','火','水','木','金','土');

  //-カレンダー表示用サブウインドウオープン
  if(winflg){

    var w = 160;
    var h = 156;
    if(clear_f) h = 160;

    //-calendar用OS別サイズ微調整
    if(Fir)     { w += 60; h += 15; }
    else if(Moz){ w += 25; h += 30; }
    else if(Win){ w +=  0; h +=  0; }
    else if(Mac){ w +=  8; h += 22; }
    else if(X11){ w +=  5; h += 46; }

    var x = 100;
    var y = 20;

    //-表示位置調整
    if(document.all){
        //e4,e5,e6
        x = window.event.screenX + 15;
        if(x + w > screen.width){ x = window.event.screenX - 180; }
        y = window.event.screenY - 180;
        if(y < 0){y = window.event.screenY}

    } else if (document.layers || document.getElementById){
        //n4,n6,n7,m1,o6
        x = e.screenX + 10;
        if(x + w > screen.width){ x = e.screenX - 200; }
        y = e.screenY - 200;
        if(y < 0){y = e.screenY; }

    }

    //-カレンダーウィンドウを表示
    mkSubWin('#','calendar',x,y,w,h);

  }
  //-カレンダー構築用基準日の取得
  fstday   = now;                                                 //今月の1日
  startday = fstday - ( fstday.getDay() * 1000 * 60 * 60 * 24 );  //最初の日曜日
  startday = new Date(startday);

  //-カレンダー構築用HTML
  ddata = '';
  ddata += '<html>\n';
  ddata += '<head>';
  ddata += '<meta http-equiv="Content-Type" content="text/html;charset=SHIFT_JIS">\n';
  ddata += '<title>CALENDAR</title>\n';
  ddata += '<style>\n';
  ddata += '  body { font:12px ; line-height:12px ; margin:7px }\n';
  ddata += '  th   { font:14px ; line-height:14px ; font-weight:900 }\n';
  ddata += '  td   { font:12px ; font-family:Arial; line-height:12px }\n';
  ddata += '  a    { text-decoration:none; color:#000000; font:12px; font-family:Arial; line-height:12px }\n';
if(!Moz || Fir){
  ddata += '  input{ font:10px ; font-family:Arial; line-height:10px ; padding:0px }\n';
}
  ddata += '</style>\n';
  ddata += '</head>\n';
  if(wlock){
    ddata += '<body bgcolor="' + c_body + '" onBlur="window.focus()">\n';
  }else{
    ddata += '<body bgcolor="' + c_body + '">\n';
  }

  ddata += '<form>\n';
  ddata += '  <table border="0" bgcolor="' + c_table + '" bordercolor="' + c_table + '" width="140" height="140">\n';

  //-YEAR/MONTH
  ddata += '    <tr id="trmonth" bgcolor="' + c_month + '" bordercolor="' + c_month + '" width="140" height="14">\n';
  ddata += '      <th colspan="7" width="140" height="14" align="right"><nobr><font color="' + f_month + '">';
  ddata +=          nowtitleyyyymm + '\n';

  ddata += '        <input type="button" value="<<" ';
  ddata +=                'onClick="self.opener.wrtCalendar(0,self.opener.document.';
  ddata +=                 oj.form.name+'.'+oj.name+',\''+flg+'\',-12,0)"><input \n';

  ddata += '               type="button" value="<" ';
  ddata +=                'onClick="self.opener.wrtCalendar(0,self.opener.document.';
  ddata +=                 oj.form.name+'.'+oj.name+',\''+flg+'\',-1,0)"><input \n';

  ddata += '               type=button VALUE="O" ';
  ddata +=                'onClick="self.opener.wrtCalendar(0,self.opener.document.';
  ddata +=                 oj.form.name+'.'+oj.name+',\''+flg+'\',0,0)"><input \n';

  ddata += '               type=button VALUE=">" ';
  ddata +=                'onClick="self.opener.wrtCalendar(0,self.opener.document.';
  ddata +=                 oj.form.name+'.'+oj.name+',\''+flg+'\',1,0)"><input \n';

  ddata += '               type=button VALUE=">>" ';
  ddata +=                'onClick="self.opener.wrtCalendar(0,self.opener.document.';
  ddata +=                 oj.form.name+'.'+oj.name+',\''+flg+'\',12,0)">\n';

  ddata += '      </font></nobr></th>\n';
  ddata += '    </tr>\n';

  //-WEEK
  ddata += '    <tr bgcolor="' + c_week + '" width="140" height="14">\n';

  for (i=0;i<7;i++){
    ddata += '      <th width="14" height="14"><font color="' + f_week + '">';
    ddata +=          week[i];
    ddata +=       '</font></th>\n';
  }
  ddata += '    </tr>\n';

  //-DATE
  for(j=0;j<6;j++){
    ddata += '    <tr bgcolor="' + c_date + '">\n';
    for(i=0;i<7;i++){
      nextday = startday.getTime() + (i * 1000 * 60 * 60 * 24);
      wrtday  = new Date(nextday);

      wrtdate     = wrtday.getDate();
      wrtmonth    = wrtday.getMonth();
      wrtyear     = wrtday.getFullYear();
      if(wrtyear < 1900) wrtyear = 1900 + wrtyear;
      wrtyyyymm   = wrtyear * 100 + wrtmonth;

      wrtyyyymmdd = makeDate(flg,wrtyear,wrtmonth,wrtdate,i);

      wrtdateA  = '<a href="javascript:function v(){';
      wrtdateA += '   self.opener.document.' + oj.form.name;
      wrtdateA += '.' + oj.name + '.value=(\'' + wrtyyyymmdd + '\'); self.close()}; v()"';
      wrtdateA += '>';
      wrtdateA += '<font color="' + f_date + '">';
      wrtdateA += wrtdate;
      wrtdateA += '</font>';
      wrtdateA += '</a>';

      if(wrtyyyymm != nowyyyymm){
        ddata += '      <td bgcolor="' + c_datex + '" width="14" height="14" align="center" valign="middle">';
        ddata += wrtdateA;

      } else if( wrtdate  == absnow.getDate()  &&
                 wrtmonth == absnow.getMonth() &&
                 wrtday.getFullYear() == absnow.getFullYear()){
        ddata += '      <td bgcolor="' + c_dateo + '" width="14" height="14" align="center" valign="middle">';
        ddata += wrtdateA;
        if(i == 1) ++moncnt;    // 月曜日をカウントする

      } else {
        // 祝日の取得
        syuku = getNationalHoliday(wrtyear,wrtmonth + 1,wrtdate,i);
        ddata += '      <td ';
        if(syuku || !i) ddata += 'bgcolor="'+s_date+'" ';       // 日祝日
        if(!syuku && i == 6) ddata += 'bgcolor="'+t_date+'" ';  // 土曜日
        ddata += 'width="14" height="14" align="center" valign="middle">';
        if(syuku){
          ddata += '<span title="'+ syuku + '">' + wrtdateA + '</span>';
        }else{
          ddata += wrtdateA;
        }
      }
      ddata += '</td>\n';
    }
    ddata += '    </tr>\n';

    startday = new Date(nextday);
    startday = startday.getTime() + (1000 * 60 * 60 * 24);
    startday = new Date(startday);
  }

  //-mac用クローズボタン
  if(Mac){
    ddata += '    <tr>\n';
    ddata += '      <td colspan="7" align="center">';
    ddata +=         '<input type="button" value="CLOSE" ';
    ddata +=                'onClick="self.close();return false">';
    ddata +=       '</td>\n';
    ddata += '    </tr>\n';
  }

  ddata += '  </table>\n';

  if(clear_f){
// sasaki add start 2005/07/15
      ddata += '<a href="javascript:function v(){';
      ddata += '   self.opener.document.' + oj.form.name;
      ddata += '.' + oj.name + '.value=(\'' + '\'); self.close()}; v()"';
      ddata += '>';
      ddata += '[CLEAR]';
      ddata += '</a>';
// sasaki add end 2005/07/15
  }
  ddata += '</form>\n';

  ddata += '</body>\n';
  ddata += '</html>\n';

  calendarwin.document.write(ddata);
  calendarwin.document.close();
  calendarwin.focus();

  winflg=1;
}

/***************************************************************************************************
 * 簡易サブウインドウ開き
 *
 *  Syntax : mkSubWin(URL,winName,x,y,w,h)
 *  例     : mkSubWin(winIndex,'test.htm','win0',100,200,150,300)
 *
 ***************************************************************************************************/

var calendarwin;

function mkSubWin(URL,winName,x,y,w,h){

    var para = ""
             + " left="        +x
             + ",screenX="     +x
             + ",top="         +y
             + ",screenY="     +y
             + ",toolbar="     +0
             + ",location="    +0
             + ",directories=" +0
             + ",status="      +0
             + ",menubar="     +0
             + ",scrollbars="  +0
             + ",resizable="   +1
             + ",innerWidth="  +w
             + ",innerHeight=" +h
             + ",width="       +w
             + ",height="      +h;

    calendarwin=window.open(URL,winName,para);
    calendarwin.focus();

}

/***************************************************************************************************
 * 祭日の取得
 *
 *  引数 : year、month、day、week
 *
 *  戻値 : 祭日の場合は祭日名、そうでなければNULL
 *
 ***************************************************************************************************/

var moncnt = 0;
var furi   = 0;
var ck     = 0;
var Syunbunpar1 = new Array(19.8277,20.8357,20.8431,21.8510);  // 春分・秋分の日付計算用1980-2099
var Syunbunpar2 = new Array(22.2588,23.2588,23.2488,24.2488);  // 春分・秋分の日付計算用1980-2099

function getNationalHoliday(year,month,day,week){
  // 変数の初期化
  syuku = '';
  if(day == 1 && moncnt > 0 && !ck) moncnt = 0;

  // ハッピーマンデーと振替休日
  if(week == 1){
    if(!ck) ++moncnt;
    // 振替休日
    // (2006年まで)「国民の祝日」が日曜日にあたるときは、その翌日を休日とする。
    if(furi == 1 && year <= 2006){
      syuku = '振替休日';   // 振替フラグが立っていたら休み
      furi = 0;
    }
    // 第2月曜
    if(moncnt == 2){
      if(month ==  1){ syuku = '成人の日'; }    // 1月
      if(month == 10){ syuku = '体育の日'; }    // 10月
    }
    // 第3月曜
    if(moncnt == 3){
      if(year >= 2003 && month == 7){ syuku = '海の日'; }   // 7月(2003～)
      if(year >= 2003 && month == 9){ syuku = '敬老の日'; } // 9月(2003～)
    }
  }

  // 春分の日・秋分の日
  var i,tyear;
  if ((year >= 1851) && (year <= 1899)) i = 0;
  else if ((year >= 1900) && (year <= 1979)) i = 1;
  else if ((year >= 1980) && (year <= 2099)) i = 2;
  else if ((year >= 2100) && (year <= 2150)) i = 3;
  else i = 4;   // 範囲外
  if(i < 4){
    if(i < 2) tyear = 1983; else tyear = 1980;
    tyear = (year - tyear);
    if(month == 3){      // 春分の日
      if(day == Math.floor(Syunbunpar1[i] + 0.242194 * tyear - Math.floor((tyear + 0.1)/4))) syuku = '春分の日';
    }else if(month == 9){ // 秋分の日
      if(day == Math.floor(Syunbunpar2[i] + 0.242194 * tyear - Math.floor((tyear + 0.1)/4))) syuku = '秋分の日';
    }
  }

  // その他の祝日
  if(month == 1 && day ==  1){ syuku = '元日' ;}            //  1月 1日
  if(month == 2 && day == 11){ syuku = '建国記念の日'; }    //  2月11日
  if(month == 4 && day == 29 && year <= 2006){ syuku = 'みどりの日'; }      //  4月29日(2006年まで)
  if(month == 4 && day == 29 && year >= 2007){ syuku = '昭和の日'; }        //  4月29日(2007年から)
  if(month == 5 && day ==  3){ syuku = '憲法記念日'; }      //  5月 3日
  if(month == 5 && day ==  4 && year >= 2007){ syuku = 'みどりの日'; }      //  5月 4日(2007年から)
  if(month == 5 && day ==  5){ syuku = 'こどもの日'; }      //  5月 5日
  if(month == 11 && day ==  3){ syuku = '文化の日'; }       // 11月 3日
  if(month == 11 && day == 23){ syuku = '勤労感謝の日'; }   // 11月23日
  if(month == 12 && day == 23){ syuku = '天皇誕生日'; }     // 12月23日
  if(year < 2003 && month == 7 && day == 20){ syuku = '海の日'; }   // 7月20日(～2002)
  if(year < 2003 && month == 9 && day == 15){ syuku = '敬老の日'; } //  9月15日(～2002)

  // 振替休日
  // (2007年から)「国民の祝日」が日曜日に当たるときは、その日後においてその日に最も近い「国民の祝日」でない日を休日とする。
  if(furi == 1 && syuku == '' && year >= 2007){
    syuku = '振替休日';   // 振替フラグが立っていたら休み
    furi = 0;
  }else if(furi == 1 && syuku != '' && year >= 2007){
    furi = 1;             // 振替フラグが立っていて祝日の場合は振替フラグを立てる
  }else if(week == 0 && syuku != ''){
    furi = 1;             // 日曜で祝日の場合は振替フラグを立てる
  }else{
    furi = 0;
  }

  // 国民の休日(祝日に挟まれた平日)
  // (2006年まで)その前日及び翌日が「国民の祝日」である日（日曜日にあたる日及び前項に規定する休日にあたる日を除く。）は、休日とする。
  // (2007年から)その前日及び翌日が「国民の祝日」である日（「国民の祝日」でない日に限る。）は、休日とする。
  if((week > 0 && syuku == '' && !ck && year <= 2006) || (syuku == '' && !ck && syuku != '振替休日' && year >= 2007)){
    ck = 1;  //再帰呼び出しでここを通らないようにする
    // 前日と次日が祝日か確認
    // １日と末日が祝日の場合はないので日にちは単純に１を増減する
    // 曜日の設定
    bweek = week - 1; if(bweek < 0) bweek = 6;
    aweek = week + 1; if(bweek > 6) bweek = 0;
    if(getNationalHoliday(year,month,day - 1,bweek) && getNationalHoliday(year,month,day + 1,aweek)){
      syuku = '国民の休日';
    }
    ck = 0;  // フラグの初期化
  }

  return syuku;
}

/***************************************************************************************************
 * 日付の生成
 *
 *  引数 : year、month、day、week
 *
 *  戻値 : フォーマットされた日付
 *
 *  日付タイプ : 'g'    → 年号の頭文字を返します (M、T、S、H)
 *               'gg'   → 年号の先頭の 1 文字を漢字で返します (明、大、昭、平)
 *               'ggg'  → 年号を返します (明治、大正、昭和、平成)
 *               'yy'   → 西暦の年を下 2 桁の数値で返します (00 ～ 99)
 *               'yyyy' → 西暦の年を 4 桁の数値で返します (100 ～ 9999)
 *               'm'    → 月を表す数値を返します。1 桁の場合、先頭に 0 が付きません (1 ～ 12)
 *               'mm'   → 月を表す数値を返します。1 桁の場合、先頭に 0 が付きます (01 ～ 12)
 *               'd'    → 日付を返します。1 桁の場合、先頭に 0 が付きません (1 ～ 31)
 *               'dd'   → 日付を返します。1 桁の場合、先頭に 0 が付きます (01 ～ 31)
 *               'w'    → 曜日を英語 (省略形) で返します (Sun ～ Sat)
 *               'ww    → 曜日を英語で返します (Sunday ～ Saturday)
 *               'a     → 曜日を日本語 (省略形) で返します (日～土)
 *               'aa    → 曜日を日本語で返します (日曜日～土曜日)
 *
 *  備考 : ・当スクリプトでは 1868/9/8 以降を「明治」と表示する。
 *
 *         ・明治45年7月30日と大正元年7月30日はともに存在する為、
 *           当スクリプトでは 1912/7/30 以降を「大正」と表示する。
 *
 *         ・大正15年12月25日と昭和元年12月25日はともに存在する為、
 *           当スクリプトでは 1926/12/25 以降を「昭和」と表示する。
 *
 *         ・元号を改める政令によると昭和64年は1月7日まで、平成元年は1月8日から
 *           よって、当スクリプトでは 1989/1/8 以降を「平成」と表示する。
 *
 ***************************************************************************************************/

function makeDate(inpt,year,month,day,i){
  month++;
  week1 = new Array('Sunday','Monday','Tuesday','Wednesday','Thursday','Friday','Saturday');
  week2 = new Array('Sun','Mon','Tue','Wed','Thu','Fri','Sat');
  week3 = new Array('日曜日','月曜日','火曜日','水曜日','木曜日','金曜日','土曜日');
  week4 = new Array('日','月','火','水','木','金','土');

  // 年
  if(inpt.match(/g/i)){
    if(year > 1988 && (month + '/' + day) > '1/7'){
      year = year - 1988;
      if(year == 1) year = '元';
      if     (inpt.match(/ggg/i)) year = '平成'+ year;
      else if(inpt.match(/gg/i))  year = '平'+ year;
      else if(inpt.match(/g/i))   year = 'H'+ year;
    }else if(year > 1925 && (month + '/' + day) > '12/24'){
      year = year - 1925;
      if(year == 1) year = '元';
      if     (inpt.match(/ggg/i)) year = '昭和'+ year;
      else if(inpt.match(/gg/i))  year = '昭'+ year;
      else if(inpt.match(/g/i))   year = 'S'+ year;
    }else if(year > 1911 && (month + '/' + day) > '7/29'){
      year = year - 1911;
      if(year == 1) year = '元';
      if     (inpt.match(/ggg/i)) year = '大正'+ year;
      else if(inpt.match(/gg/i))  year = '大'+ year;
      else if(inpt.match(/g/i))   year = 'T'+ year;
    }else if(year > 1867 && (month + '/' + day) > '9/7'){
      year = year - 1867;
      if(year == 1) year = '元';
      if     (inpt.match(/ggg/i)) year = '明治'+ year;
      else if(inpt.match(/gg/i))  year = '明'+ year;
      else if(inpt.match(/g/i))   year = 'M'+ year;
    }
    // 年の置き換え
    inpt = inpt.replace('ggg', year); inpt = inpt.replace('gg', year); inpt = inpt.replace('g', year);

  }else{
    // 年の置き換え
    inpt = inpt.replace('yyyy', year);
    inpt = inpt.replace('yy', (year+'').substr(2, 2));
  }

  // 月
  if(inpt.match(/mm/i)){
    if(month < 10) month = '0' + month;
  }
  // 月の置き換え
  inpt = inpt.replace('mm', month); inpt = inpt.replace('m', month);

  // 日
  if(inpt.match(/dd/i)){
    if(day < 10)   day   = '0' + day;
  }
  // 日の置き換え
  inpt = inpt.replace('dd', day); inpt = inpt.replace('d', day);

  // 曜日の置き換え
  if     (inpt.match(/ww/i)) inpt = inpt.replace('ww', week1[i]);
  else if(inpt.match(/w/i))  inpt = inpt.replace('w',  week2[i]);
  else if(inpt.match(/aa/i)) inpt = inpt.replace('aa', week3[i]);
  else if(inpt.match(/a/i))  inpt = inpt.replace('a',  week4[i]);

  return inpt;
}

/***************************************************************************************************/
