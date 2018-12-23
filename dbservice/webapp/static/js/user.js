function fsign_up(username, password) {
	console.log(username, password);
	$.ajax({
		type : 'POST',
		url : '/user/sign/up',
		data : JSON.stringify({
			'name' : username,
			'password' : password
		}),
		dataType : 'JSON',
		contentType : 'application/json;charset=UTF-8',
		success : function(data) {
			console.log(data);
			$form_modal.removeClass('is-visible');
			if (data.status == "success") {
				swal('注册成功', '请点击右上角登陆', 'success');
			} else {
				swal('错误', data.msg, 'error');
			}

		},
		error : function() {
			$form_modal.removeClass('is-visible');
			swal('错误', '请检查网络后重试，或联系管理员', 'error');
		}
	})
}

function fsign_in(username, password) {
	$form_modal.removeClass('is-visible');
	console.log(username, password);
	$.ajax({
		type : 'POST',
		url : '/user/sign/in',
		data : JSON.stringify({
			'name' : username,
			'password' : password
		}),
		dataType : 'JSON',
		contentType : 'application/json;charset=UTF-8',
		success : function(data) {
			if(data.status == 'success'){
				$.cookie('token', data.data.token, {expires: 7, path: '/' });
				$.cookie('user', data.data.user.name, {expires: 7, path: '/' });
				$.cookie('userId', data.data.user.id, {expires: 7, path: '/' });
				console.log(data.data.token);
				console.log(data.data.user.name);
				swal('登陆成功', '', 'success').then((result) => {
					window.location.href = '/editor?token='+$.cookie('token');
				})
			}
			console.log(data);
		},
		error : function() {
			swal('错误', '请检查网络后重试，或联系管理员', 'error')
		}
	})
}

function get_user_info(token) {
	$.ajax({
		type : 'GET',
		url : '/user/info?token='+token,
		success : function(data) {
			
		}
	})
}


