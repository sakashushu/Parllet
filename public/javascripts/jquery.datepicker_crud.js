jQuery(function() {
	jQuery(".datepicker").datepicker({
		changeMonth: true,
		numberOfMonths: 1,
		showOtherMonths: true,
		selectOtherMonths: true,
		showAnim: "drop",
		changeMonth: true,
		changeYear: true,
		yearRange: '1900:2999',
		dateFormat: 'yy-mm-dd'
	});
});
