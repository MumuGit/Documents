$(document).ready(function () {
    //add link
    var fileref = document.createElement('link');
    fileref.setAttribute("type", "text/css");
    fileref.setAttribute("rel", "stylesheet");
    fileref.setAttribute("href", "http://files.cnblogs.com/files/yc-755909659/bootstrap.css");
    document.getElementsByTagName("head")[0].appendChild(fileref);
    //add meta
    var fileref2 = document.createElement('meta');
    fileref2.setAttribute("http-equiv", "x-ua-compatible");
    fileref2.setAttribute("content", "ie=edge");
    document.getElementsByTagName("head")[0].appendChild(fileref2);
    var fileref3 = document.createElement('meta');
    fileref3.setAttribute("name", "keywords");
    fileref3.setAttribute("content", "叶超Luka,叶超Luka,叶超Luka's Blog,叶小超,Web,Web前端,Android");
    document.getElementsByTagName("head")[0].appendChild(fileref3);
    var fileref4 = document.createElement('meta');
    fileref4.setAttribute("name", "description");
    fileref4.setAttribute("content", "This is 叶超Luka's Blog");
    document.getElementsByTagName("head")[0].appendChild(fileref4);
    //delete element
    $('#blogTitle').remove();
    $('.blogStats').remove();
    //add class
    $("#navigator").addClass("navbar navbar-default");
    //add element
    var navbar_header = document.createElement("div");
    navbar_header.setAttribute("class", "container");
    $("#navList").wrap($(navbar_header));
    $("#navList").before("<div class='navbar-header'><button type='button' class='navbar-toggle' data-toggle='collapse' data-target='.navbar-collapse'><span class='icon-bar'></span><span class='icon-bar'></span><span class='icon-bar'></span><span class='icon-bar'></span><span class='icon-bar'></span><span class='icon-bar'></span></button><a class='navbar-brand' href='http://www.cnblogs.com/yc-755909659/'>叶超Luka's Blog</a></div>");
    $("#navList").before("");
    var navbar_collapse = document.createElement("div");
    navbar_collapse.setAttribute("class", "navbar-collapse collapse");
    $("#navList").wrap($(navbar_collapse));
    $("#navList").addClass("nav navbar-nav");
    //Baidu tongji
    var _hmt = _hmt || [];
    (function () {
        var hm = document.createElement("script");
        hm.src = "//hm.baidu.com/hm.js?64e30bc100ae5325a1efe9bed3f46081";
        var s = document.getElementsByTagName("script")[0];
        s.parentNode.insertBefore(hm, s);
    })();
    ///////
    $('#goup').goup();
    var h = $(window).height();
    $('#scroll a').click(function () {
        $("html, body").animate({ scrollTop: h }, 900);
        return false;
    });
    $('#divheader').height(h);

    var h6 = parseInt(h / 6);
    $('#title').height(h6);
    $('#twitter').height(h6);
    $('#downloadbuttons').height(h6);
    $('#scroll').height(h6);


    $(".postTitle a").hover(function () {
        $(this).stop().animate({ paddingLeft: '10px' }, 250)
    }, function () {
        $(this).stop().animate({ paddingLeft: '0px' }, 250)
    })
});
//本地图片预览
function setImagePreview(fieldupload, image, imagediv) {
    var docObj = document.getElementById(fieldupload);
    var imgObjPreview = document.getElementById(image);
    if (docObj.files && docObj.files[0]) {
        //火狐下,直接设img属性  
        imgObjPreview.style.display = 'block';
        imgObjPreview.style.width = '150px';
        imgObjPreview.style.height = '150px';
        //imgObjPreview.src = docObj.files[0].getAsDataURL();      
        //火狐7以上版本不能用上面的getAsDataURL()方式获取，需要一下方式          
        imgObjPreview.src = window.URL.createObjectURL(docObj.files[0]);
    } else {
        //IE下，使用滤镜     
        docObj.select();
        var imgSrc = document.selection.createRange().text;
        var localImagId = document.getElementById(imagediv);
        //必须设置初始大小
        localImagId.style.width = "150px";
        localImagId.style.height = "150px";
        //图片异常的捕捉，防止用户修改后缀来伪造图片
        try {
            localImagId.style.filter = "progid:DXImageTransform.Microsoft.AlphaImageLoader(sizingMethod=scale)"; localImagId.filters.item("DXImageTransform.Microsoft.AlphaImageLoader").src = imgSrc;
        } catch (e) {
            alert("您上传的图片格式不正确,请重新选择!!");
            return false;
        }
        imgObjPreview.style.display = 'none';
        document.selection.empty();
    }
    return true;
}