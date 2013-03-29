jQuery(function() {
	jQuery(".datetimepicker").datetimepicker({
		changeMonth: true,
		numberOfMonths: 1,
		showOtherMonths: true,
		selectOtherMonths: true,
		showAnim: "drop",
		changeMonth: true,
		changeYear: true,
		yearRange: '1900:2999',
		dateFormat: 'yy/mm/dd',
		currentText: '現在日時',
		closeText: '閉じる',
		timeText: '時刻',
		hourText: '時',
		minuteText: '分'
	});
});
