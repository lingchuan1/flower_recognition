/*!
 * bootstrap-imgupload v2.0.0 2016/03/25
 * https://github.com/egonolieux/bootstrap-imgupload
 * Copyright 2016 Egon Olieux
 * Released under the MIT license
 */

(function($) {
    var options = {
        allowedFormats: [ "jpg", "jpeg", "png", "gif" ],
        previewWidth: 250,
        previewHeight: 250,
        maxFileSizeKb: 2048
    };

    $.fn.imgupload = function(givenOptions) {
        if (this.filter("div").hasClass("imgupload")) {
            options = $.extend(options, givenOptions); 

            // Show file tab.
            this.find(".panel-heading .btn:eq(0)").click(function() {
                $(this).blur();
                showFileTab($(this));
            });

            // Submit or remove file.
            var $fileTab = this.find(".file-tab");
            $fileTab.find(".btn:eq(0)").change(function() {
                $(this).blur();
                selectImageFile($(this));
            });
            $fileTab.find(".btn:eq(1)").click(function() {
                $(this).blur();
                removeImageFile($(this));
            });

            // Show URL tab.
            this.find(".panel-heading .btn:eq(1)").click(function() {
                $(this).blur();
                showUrlTab($(this));
            });

            // Submit URL.
            var $urlTab = this.find(".url-tab");
            $urlTab.find(".btn:eq(0)").click(function() {
                $(this).blur();
                selectImageUrl($(this));
            });
        }
        return this;
    };

    function getHtmlErrorMessage(message) {
        var html = [];
        html.push("<div class='alert alert-danger alert-dismissible'>");
        html.push("<button type='button' class='close' data-dismiss='alert'>");
        html.push("<span>&times;</span>");
        html.push("</button>" + message);
        html.push("</div>");
        return html.join("");
    }

    function getHtmlImage(src) {
        return "<img src='" + src + "' alt='Image preview' class='img-thumbnail' style='max-width: " + options.previewWidth + "px; max-height: " + options.previewHeight + "px'>";
    }

    function showFileTab($button) {
        if (!$button.hasClass("active")) {
            var $imgupload = $button.closest(".imgupload");
            var $urlTab = $imgupload.find(".url-tab");

            // Set file button active and show file tab.
            $button.addClass("active");
            $imgupload.find(".file-tab").show();
            $imgupload.find(".panel-heading .btn:eq(1)").removeClass("active");
            $urlTab.hide();

            // Reset URL tab.
            $urlTab.find(".alert").remove();
            $urlTab.find("input").val("");
            $.each($urlTab.find(".btn"), function(key, value) {
                if ($(value).text() == "Remove") {
                    $(value).remove();
                }
                else {
                    $(value).text("Submit");
                }
            });
            $urlTab.find("img").remove();
        }
    }

    function selectImageFile($button) {
        var $input = $button.find("input");
        var $fileTab = $button.closest(".file-tab");
        
        // Remove errors and previous image.
        $fileTab.find(".alert").remove();
        $fileTab.find("img").remove();

        // Check if file was uploaded.
        var hasFile = $input[0].files && $input[0].files[0];
        if (!hasFile) {
            return;
        }

        // Check file size.
        var file = $input[0].files[0];
        if (file.size / 1024 > options.maxFileSizeKb)
        {
            $input.val("");
            $fileTab.append(getHtmlErrorMessage("File size is too large (max " + options.maxFileSizeKb + "kB)."));
            return;
        }

        // Check image format by file extension.
        var fileExtension = file.name.substr(file.name.lastIndexOf('.') + 1).toLowerCase();
        if ($.inArray(fileExtension, options.allowedFormats) > -1) {
            var reader = new FileReader();
            reader.onload = function(e) {
                $fileTab.append(getHtmlImage(e.target.result));
            };
            reader.onerror = function(e) {
                $input.val("");
                $fileTab.append(getHtmlErrorMessage("Error loading image."));
            };
            reader.readAsDataURL(file);

            // Change submit button text to 'change' and show remove button.
            $button.find("span").text("Change");
            $fileTab.find(".btn:eq(1)").css("display", "inline-block");
        }
        else {
            $input.val("");
            $fileTab.append(getHtmlErrorMessage("Image format is not allowed."));
        }
    }

    function removeImageFile($button) {
        var $fileTab = $button.closest(".file-tab");
        $fileTab.find(".alert").remove();
        $fileTab.find(".btn span").text("Browse");
        $fileTab.find("input").val("");
        $fileTab.find("img").remove();
        $button.hide();
    }

    function showUrlTab($button) {
        if (!$button.hasClass("active")) {
            var $imgupload = $button.closest(".imgupload");
            var $fileTab = $imgupload.find(".file-tab");

            // Set URL button active and show URL tab.
            $button.addClass("active");
            $imgupload.find(".url-tab").show();
            $imgupload.find(".panel-heading .btn:eq(0)").removeClass("active");
            $fileTab.hide();

            // Reset file tab.
            $fileTab.find(".alert").remove();
            $fileTab.find("input").val("");
            $fileTab.find(".btn span").text("Browse");
            $fileTab.find(".btn:eq(1)").hide();
            $fileTab.find("img").remove();
        }
    }

    function selectImageUrl($button) {
        var $urlTab = $button.closest(".url-tab");
        var $textInput = $urlTab.find("input:text");
        var $buttons = $urlTab.find(".btn");

        // Disable input.
        $textInput.prop("disabled", true);
        $buttons.prop("disabled", true);

        // Remove errors and previous image.
        $urlTab.find(".alert").remove();
        $urlTab.find("img").remove();

        isValidImageUrl($urlTab.find("input:text").val(), function(url, isValid, message) {
            if (isValid) {
                $urlTab.find("input:hidden").val(url);
                $urlTab.append(getHtmlImage(url));

                if ($buttons.length == 1) {
                    $urlTab.find(".input-group-btn").append("<button type='button' class='btn btn-default'>Remove</button>");

                    // Remove URL (and button).
                    $urlTab.find(".btn:eq(1)").click(function() {
                        $(this).blur();
                        $urlTab.find(".alert").remove();
                        $urlTab.find("input").val("");
                        $urlTab.find("img").remove();
                        $(this).remove();
                    });
                }
            }
            else {
                if ($buttons.length == 2) {
                    $buttons[1].remove();
                }
                $urlTab.append(getHtmlErrorMessage(message));
            }

            // Enable input.
            $textInput.prop("disabled", false);
            $buttons.prop("disabled", false);
        });
    }

    function isValidImageUrl(url, callback) {
        var image = new Image();
        image.onerror = function() {
            callback(url, false, "Image could not be found.");
        };
        image.onload =  function() {
            // Check image format by file extension.
            var fileExtension = url.substr(url.lastIndexOf('.') + 1).toLowerCase();
            if ($.inArray(fileExtension, options.allowedFormats) > -1) {
                callback(url, true, "");
            }
            else {
                callback(url, false, "Image format is not allowed.");
            }
        };
        image.src = url;
    }
}(jQuery));
