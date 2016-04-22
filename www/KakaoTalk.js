var exec = require('cordova/exec');

var KakaoTalk = {
	login: function (successCallback, errorCallback) {
		exec(successCallback, errorCallback, "KakaoTalk", "login", []);
    },
	logout: function (successCallback, errorCallback) {
		exec(successCallback, errorCallback, 'KakaoTalk', 'logout', []);
	},
	share : function(options, successCallback, errorCallback) {

		for(var options_key in options){
			if(typeof options[options_key] === 'object'){
				for(var key in options[options_key]){
					options[options_key][key] = options[options_key][key] || '';
				};
			}else{
				options[options_key] = options[options_key] || '';
			}
		};
	    exec(successCallback, errorCallback, "KakaoTalk", "share", [options]);
	}
};

module.exports = KakaoTalk;
