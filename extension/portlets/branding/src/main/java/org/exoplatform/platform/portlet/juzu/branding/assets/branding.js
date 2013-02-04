$(function() {
	var fileUpload;
	UpdatePreviewLogoAndStyle();
	$("#saveinfo").hide();
	$("#cancelinfo").hide();
	$("#mustpng").hide();
	$("#PlatformAdminToolbarContainer").clone().appendTo($("#StylePreview"));
	fixSearchInput();
	$("#cancel").on(
			"click",
			function() {
				UpdatePreviewLogoAndStyle();
				$("#saveinfo").hide();
				$("#mustpng").hide();
				$("#cancelinfo").show();
			});
	$("#save").on("click", function() {
		$("#style").val(($('#navigationStyle option:selected').val()));
		var result = $('#form').submit();
		$("#saveinfo").show();
		$("#cancelinfo").hide();
		$("#mustpng").hide();
		return result;
	});

	$("input#file").on("change", function() {
		previewLogoFromFile(this.files[0]);
	});
	
	

	$('#form')
			.submit(
					function() {
						fd = new FormData($("#form").get(0));
						if (fileUpload) {
							fd.append("file", fileUpload);
						}
						$
								.ajax({
									type : "POST",
									url : $("#form").attr("action"),
									data : fd,
									beforeSend: function(){
										$("#PreviewImg").hide();
										$("#StylePreview #PlatformAdminToolbarContainer").hide();
										$("#ajaxUploading1").show();
										$("#ajaxUploading2").show();
									},
									dataType : "json", 
									contentType : false,
									processData : false,
									success : function(data) {
										$("#ajaxUploading1").hide();
										$("#ajaxUploading2").hide();
										$("#PreviewImg").show();
										$("#StylePreview #PlatformAdminToolbarContainer").show();
										UpdateTopBarNavigation(data);
										fileUpload==null;
										$("#saveinfo").show();
										$("#cancelinfo").hide();
										$("#mustpng").hide();
									}
								});
						return false;
					});

	/* Change CSS by selecting */
	$("#navigationStyle").change(function() {
		var style = $("#navigationStyle").val();
		changePreviewStyle(style);

	});

	function changePreviewStyle(style) {
		var idContainer = $("#StylePreview #UIToolbarContainer");
		idContainer.removeAttr("class");
		idContainer.addClass("UIToolbarContainer" + style + " UIContainer");
	}

	function fixSearchInput() {
		$(
				"#StylePreview #UIToolbarContainer #SearchNavigationTabsContainer input")
				.remove();
	}
	function previewLogoFromFile(file) {
		var checkValide = validate(file);
		if (checkValide == false) {
			$("#saveinfo").hide();
			$("#cancelinfo").hide();
			$("#mustpng").show();
			return;
		} else {
			fileUpload = file;
			var reader = new FileReader();
			reader.onload = function(e) {
				previewLogoFromUrl(e.target.result);
			};
			reader.readAsDataURL(file);
		}
	}

	function validate(file) {
		if (file.type == "image/png") {
			return true;
		} else {
			return false;
		}
	}

	function scaleToFitPreviewImg(elt) {
		$(elt).imgscale({
			parent : '#PreviewImgDiv',
			fade : 1000
		});
	}

	function previewLogoFromUrl(logoUrl) {
		$('#PreviewImg').attr('src', logoUrl);
		scaleToFitPreviewImg($('#PreviewImg'));
		$('#StylePreview #HomeLink img').attr('src', logoUrl).width(25).height(21);
	}

	function UpdatePreviewLogoAndStyle() {
		$("#navigationStyle").jzAjax(
				{
					url : "BrandingControler.getResource()",
					beforeSend : function() {
					},
					success : function(data) {
						// update the logo url in preview zone and preview
						// navigation bar
						previewLogoFromUrl(data.logoUrl);
						// update the navigation style and style selected;
						changePreviewStyle(data.style);
						$("#navigationStyle").val(data.style).attr('selected',
								'selected');
					}
				});
	}
	function UpdateTopBarNavigation(data) {
		$("#PlatformAdminToolbarContainer .HomeLink img:first").attr('src',
				data.logoUrl).width(25).height(21);
		$("#PlatformAdminToolbarContainer #UIToolbarContainer:first")
				.removeAttr("class");
		$("#PlatformAdminToolbarContainer #UIToolbarContainer:first").addClass(
				"UIToolbarContainer" + data.style + " UIContainer");
	}

	function FileDragHover(e) {
		e.stopPropagation();
		e.preventDefault();
		e.target.className = (e.type == "dragover" ? "hover" : "");
	}

	// file selection
	function FileDropHandle(e) {
		e.stopPropagation();
		e.preventDefault();
		e.target.className ="";
		var files = e.target.files || e.dataTransfer.files;
		previewLogoFromFile(files[0]);
	}
	
	if (window.File && window.FileList && window.FileReader) {
		var filedrag =document.getElementById("PreviewImgDiv");
		var xhr = new XMLHttpRequest();
		if (xhr.upload) {
			filedrag.addEventListener("dragover", FileDragHover, false);
			filedrag.addEventListener("dragleave", FileDragHover, false);
			filedrag.addEventListener("drop", FileDropHandle, false);
			filedrag.style.display = "block";
		}
	}

});
