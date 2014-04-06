jQuery(function() {
	var dates = jQuery("#from, #to").datepicker({
		defaultDate: "+1w",
		changeMonth: true,
		numberOfMonths: 1,
		showOtherMonths: true,
		selectOtherMonths: true,
		showAnim: "drop",
		changeMonth: true,
		changeYear: true,
		yearRange: '1900:2999',
		dateFormat: 'yy/mm/dd',
		onSelect: function( selectedDate ) {
			var option = this.id == "from" ? "minDate" : "maxDate",
				instance = jQuery( this ).data( "datepicker" ),
				date = jQuery.datepicker.parseDate(
					instance.settings.dateFormat ||
					jQuery.datepicker._defaults.dateFormat,
					selectedDate, instance.settings );
			dates.not( this ).datepicker( "option", option, date );
		},
		showButtonPanel: true,
	});
});

jQuery(function() {
	var dates = jQuery("#from2, #to2").datepicker({
		defaultDate: "+1w",
		changeMonth: true,
		numberOfMonths: 1,
		showOtherMonths: true,
		selectOtherMonths: true,
		showAnim: "drop",
		changeMonth: true,
		changeYear: true,
		yearRange: '1900:2999',
		dateFormat: 'yy/mm/dd',
		onSelect: function( selectedDate ) {
			var option = this.id == "from2" ? "minDate" : "maxDate",
				instance = jQuery( this ).data( "datepicker" ),
				date = jQuery.datepicker.parseDate(
					instance.settings.dateFormat ||
					jQuery.datepicker._defaults.dateFormat,
					selectedDate, instance.settings );
			dates.not( this ).datepicker( "option", option, date );
		},
		showButtonPanel: true
	});
});

jQuery(function() {
//	jQuery(".datepicker").datepicker({
//		changeMonth: true,
//		numberOfMonths: 1,
//		showOtherMonths: true,
//		selectOtherMonths: true,
//		showAnim: "drop",
//		changeMonth: true,
//		changeYear: true,
//		yearRange: '1900:2999',
//		dateFormat: 'yy/mm/dd',
//		showButtonPanel: true,
//		onSelect: function(date) {
//			
//		}
//	});
	var datepicker = jQuery(".datepicker");
	datepicker.datepicker({
		changeMonth: true,
		numberOfMonths: 1,
		showOtherMonths: true,
		selectOtherMonths: true,
		showAnim: "drop",
		changeMonth: true,
		changeYear: true,
		yearRange: '1900:2999',
		dateFormat: 'yy/mm/dd',
		showButtonPanel: true,
		onSelect: function(date) {
			
		}
	});
	$('.ui-datepicker-current').live('click', function() {
		// set date to today and trigger the click event
		datepicker.datepicker('setDate', new Date());
		$('.ui-datepicker-current-day').click();
	});
	// trigger today button on initial page load
	$('.ui-datepicker-current').trigger('click');
});
