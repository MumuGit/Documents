/**
 * Created by fengjj on 2017/5/25.
 * 上报用户是否安装广告拦截插件
 */
(function(window,undefined){
    function adBlockDetected() {
        var img = new Image();
        img.onload =img.onerror = function(){
            img.onload = img.onerror = null;
            img = null;
        }
        img.src = "http://csdnimg.cn/Judiciary.png";

    }
    if(typeof fuckAdBlock === 'undefined') {
        adBlockDetected();
    } else {
        fuckAdBlock.on(true, adBlockDetected);
    }
})(window)