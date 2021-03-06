function $$(id) {
    return  document.getElementById(id);
}

var defaultTextColor = "#888888";

function checkRegisterForm() {
    if (checkEmail() && checkCode() && checkPassword() && checkConfirmPassword()
            && checkName() && checkPhone() && checkPassword() && checkProvince()
            && checkCity() && checkArea() && checkVerificationCode()) {
        return true;
    } else {
        alert("某些信息输入不正确，请检查红字部分！");
        return false;
    }
}

function checkResetPasswordForm() {
    if (checkEmail() && checkCode() && checkPassword() && checkConfirmPassword() && checkVerificationCode()) {
        return true;
    } else {
        alert("某些信息输入不正确，请检查红字部分！");
        return false;
    }
}

function checkModifyInformationForm() {
    if (checkName() && checkPhone() && checkProvince() && checkCity() && checkArea()) {
        return true;
    } else {
        alert("某些信息输入不正确，请检查红字部分！");
        return false;
    }
}

function checkCreateClassForm() {
    if (checkClassName() && checkSchool() && checkYear() && checkProvince() && checkCity() && checkArea()) {
        return true;
    } else {
        alert("某些信息输入不正确，请检查红字部分！");
        return false;
    }
}

function checkExitClassForm(form) {
    if (form.user_id.value == form.manager_id.value) {
        alert("班级管理者不能进行此操作！");
        return false;
    }
    return true;
}

function checkRemoveClassmate(form) {
    if (form.user_id.value == form.manager_id.value) {
        alert("班级管理者不能删除自己！");
        return false;
    }
    return true;
}

function checkRemoveMessage(form) {
    if (form.user_id.value != form.manager_id.value && form.user_id.value != form.msg_user_id.value) {
        alert("非班级管理员不能删除他人的留言");
        return false;
    }
    return true;
}

function checkEmail() {
    var reg = /^\s*\w+(?:\.{0,1}[\w-]+)*@[a-zA-Z0-9]+(?:[-.][a-zA-Z0-9]+)*\.[a-zA-Z]+\s*$/;
    var emailStr = $$('email').value;
    if (emailStr != null && emailStr != '' && reg.test(emailStr)) {
        $$('email').style.color=defaultTextColor;
        return true;
    } else {
        $$('email').style.color="#FF0000";
        return false;
    }
}

function checkCode() {
    var reg = /^[0-9]*$/
    var codeStr =  $$('code').value;
    if (codeStr.length == 6 && reg.test(codeStr)) {
        $$('code').style.color=defaultTextColor;
        return true;
    } else {
        $$('code').style.color="#FF0000";
        return false;
    }
}

function checkPassword() {
    var first = $$('password').value;
    var second = $$('confirmPassword').value;
    if (first != null && second != null && first != ''
        && second != '' && first.length >= 8 && first.length <= 32
        && second.length >= 8 && second.length <= 32 && first == second) {
        $$('password').style.color=defaultTextColor;
        $$('confirmPassword').style.color=defaultTextColor;
        return true;
    } else {
        $$('password').style.color="#FF0000";
        return false;
    }
}

function checkConfirmPassword() {
    var first = $$('password').value;
    var second = $$('confirmPassword').value;
    if (first != null && second != null && first != ''
        && second != '' && first.length >= 8 && first.length <= 32
        && second.length >= 8 && second.length <= 32 && first == second) {
        $$('password').style.color=defaultTextColor;
        $$('confirmPassword').style.color=defaultTextColor;
        return true;
    } else {
        $$('confirmPassword').style.color="#FF0000";
        return false;
    }
}

function checkName() {
    var nameStr = $$('name').value;
    if (nameStr.length > 0) {
        $$('name').style.color=defaultTextColor;
        return true;
    } else {
        $$('name').style.color="#FF0000";
        return false;
    }
}

function checkPhone() {
    var phoneStr = $$('phone').value;
    var reg = /^[0-9]*$/;
    if (phoneStr.length == 11 && reg.test(phoneStr)) {
        $$('phone').style.color=defaultTextColor;
        return true;
    } else {
        $$('phone').style.color="#FF0000";
        return false;
    }
}

function checkProvince() {
    var province = $$('province').value;
    if (province != null && province != '0') {
        $$('province').style.color=defaultTextColor;
        return true;
    } else {
        $$('province').style.color="#FF0000";
        return false;
    }
}

function checkCity() {
    var city = $$('city').value;
    if (city != null && city != '0') {
        $$('city').style.color=defaultTextColor;
        return true;
    } else {
        $$('city').style.color="#FF0000";
        return false;
    }
}

function checkArea() {
    var area = $$('area').value;
    if (area != null && area != '0') {
        $$('area').style.color=defaultTextColor;
        return true;
    } else {
        $$('area').style.color="#FF0000";
        return false;
    }
}

function checkVerificationCode() {
    var reg = /^[0-9a-zA-Z]*$/;
    var captchaStr =  $$('captcha').value;
    if (captchaStr.length == 6 && reg.test(captchaStr)) {
        $$('captcha').style.color=defaultTextColor;
        return true;
    } else {
        $$('captcha').style.color="#FF0000";
        return false;
    }
}


function checkClassName() {
    var classNameStr = $$('className').value;
    if (classNameStr.length > 0) {
        $$('className').style.color=defaultTextColor;
        return true;
    } else {
        $$('className').style.color="#FF0000";
        return false;
    }
}


function checkSchool() {
    var schoolStr = $$('school').value;
    if (schoolStr.length > 0) {
        $$('school').style.color=defaultTextColor;
        return true;
    } else {
        $$('school').style.color="#FF0000";
        return false;
    }
}


function checkYear() {
    var reg = /^[0-9]*$/
    var yearStr = $$('gradeYear').value;
    if (yearStr.length > 0 && reg.test(yearStr)) {
        $$('gradeYear').style.color=defaultTextColor;
        return true;
    } else {
        $$('gradeYear').style.color="#FF0000";
        return false;
    }

}