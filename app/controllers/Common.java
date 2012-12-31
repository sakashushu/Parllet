package controllers;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class Common {
	/**
	 * 日付数値の妥当性チェック
	 * @param intDate
	 * @return 存在する日付の場合true
	 */
	public boolean checkIntDate(Integer intDate) {
			if(intDate<10000101 ||
					intDate>99991231)
				return false;
				
			String strTmp = intDate.toString();
			String sBasisDate = strTmp.substring(0, 4) + "/" + strTmp.substring(4, 6) + "/" + strTmp.substring(6);
			//日付の妥当性チェック
			return checkDate(sBasisDate);
	}
	
	/**
	 * 日付の妥当性チェックを行います。
	 * 指定した日付文字列（yyyy/MM/dd or yyyy-MM-dd）が
	 * カレンダーに存在するかどうかを返します。
	 * @param strDate チェック対象の文字列
	 * @return 存在する日付の場合true
	 */
	public boolean checkDate(String strDate) {
		if (strDate == null || strDate.length() != 10)
			return false;
		strDate = strDate.replace('-', '/');
//		DateFormat format = DateFormat.getDateInstance();
		DateFormat format = new SimpleDateFormat("yyyy/MM/dd");
		// 日付/時刻解析を厳密に行うかどうかを設定する。
		format.setLenient(false);
		try {
			format.parse(strDate);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
}
