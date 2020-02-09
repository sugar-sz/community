function post() {
    let question_id = $("#question_id").val();
    let content = $("#comment_content").val();
    comment2target(question_id,1,content)
}

function comment2target(targetId, type, content) {
    if(!content){
        alert("不能回复空内容")
    }
    $.ajax({
            type: "POST",
            url: "/comment",
            data: JSON.stringify({
                "parentId": targetId,
                "content": content,
                "type": type
            }),
            success: function (response) {
                if (response.code == 200) {
                    window.location.reload();
                } else {
                    if (response.code == 2003) {
                        if (confirm(response.message)) {
                            window.open("https://github.com/login/oauth/authorize?client_id=747a6bd7536c542052c2&redirect_uri=http://localhost:8080/callback&scope=user&state=1");
                            window.localStorage.setItem("closable","true");
                        } else {
                            alert(response.message);
                        }
                    }
                    console.log(response);
                }
            },
            contentType: 'application/json'
        }
    )
}

function comment(e) {
    let commentId = e.getAttribute("data-id");
    let content = $("#input-" + commentId).val();
    comment2target(commentId, 2, content);
}


function collapseComments(e) {
    let id = e.getAttribute("data-id");
    let comments = $("#comment-" + id);

    // 获取一下二级评论的展开状态
    let collapse = e.getAttribute("data-collapse");
    if (collapse) {
        // 折叠二级评论
        comments.removeClass("in");
        e.removeAttribute("data-collapse");
        e.classList.remove("active");
    } else {
        let subCommentContainer = $("#comment-" + id);
        if (subCommentContainer.children().length !== 1) {
            //展开二级评论
            comments.addClass("in");
            // 标记二级评论展开状态
            e.setAttribute("data-collapse", "in");
            e.classList.add("active");
        } else {
            $.getJSON("/comment/" + id, function (data) {
                console.log(data);
                $.each(data.data.reverse(), function (index, comment) {
                    let mediaLeftElement = $("<div/>", {
                        "class": "media-left"
                    }).append($("<img/>", {
                        "class": "media-object img-rounded",
                        "src": comment.user.avatarUrl
                    }));

                    let mediaBodyElement = $("<div/>", {
                        "class": "media-body"
                    }).append($("<h5/>", {
                        "class": "media-heading",
                        "html": comment.user.name
                    })).append($("<div/>", {
                        "html": comment.content
                    })).append($("<div/>", {
                        "class": "menu"
                    }).append($("<span/>", {
                        "class": "pull-right",
                        "html": moment(comment.gmtCreate).format('YYYY-MM-DD hh:mm:ss')
                    })));

                    let mediaElement = $("<div/>", {
                        "class": "media"
                    }).append(mediaLeftElement).append(mediaBodyElement);

                    let commentElement = $("<div/>", {
                        "class": "col-lg-12 col-md-12 col-sm-12 col-xs-12 comments"
                    }).append(mediaElement);

                    subCommentContainer.prepend(commentElement);
                });
                //展开二级评论
                comments.addClass("in");
                // 标记二级评论展开状态
                e.setAttribute("data-collapse", "in");
                e.classList.add("active");
            });
        }
    }
}

function showSelectTag() {
    $("#select-tag").show();
}

function selectTag(e) {
    var value = e.getAttribute("data-tag");
    var previous = $("#tag").val();
    if (previous.indexOf(value) == -1) {
        if (previous) {
            $("#tag").val(previous + ',' + value);
        } else {
            $("#tag").val(value);
        }
    }
}