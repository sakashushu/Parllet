jQuery(function(){
	jQuery('#cnf_del').click(myObj, function(eo){
	    return confirm(eo.data.strMsg);
	});
});
