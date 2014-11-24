(function($){
  var NotificationPopover = {
      popupId : 'NotificationPopup',
      maxItem : 13,
      popupItem : null,
      defaultAvatar : '/social-resources/skin/images/ShareImages/UserAvtDefault.png',
      init : function() {
        $('#UICreatePlatformToolBarPortlet').find('.dropdown-toggle:first')
        .on('click', function() { console.log()});
        
        //
        var socketUrl = 'ws://' + location.hostname + ':8181/channels/notification-web/' + window.eXo.env.portal.userName;
        var socket = new WebSocket(socketUrl);
        socket.onmessage = function(evt) {
          var obj = JSON.parse(evt.data);
          NotificationPopover.appendMessage(obj.message);
        }
        socket.onopen = function(evt) {
          if (socket.readyState == WebSocket.OPEN) {
            socket
                .send('{"action": "subscribe", "identifier" : "notification-web"}');
          } else {
            window.console.log("The socket is not open.");
          }
        }
  
        socket.onclose = function(evt) {
          window.console.log("Web Socket closed.");
        }
        //
        NotificationPopover.popupItem = $('#' + NotificationPopover.popupId).find('ul.displayItems:first');
        NotificationPopover.template = $('#' + NotificationPopover.popupId).find('li.template:first')
        
      },
      appendMessage : function(message) {
        var newItem = NotificationPopover.template.clone();
        newItem.find('img:first').attr('src', NotificationPopover.defaultAvatar);
        newItem.find('.contentSmall:first').html(message);
        newItem.find('.contentTime:first').html('2 minutes ago');
        newItem.find('a.remove-item:first').attr('id', 'message' + (new Date().getTime()));
        //
        var target = $('<ul></ul>').append(NotificationPopover.popupItem.find('li'));
        //
        NotificationPopover.popupItem.append(newItem);
        //
        target.find('li').each(function(i){
          if((i + 1) < NotificationPopover.maxItem) {
            NotificationPopover.popupItem.append($(this));
          }
        });
        target.remove();
        //
        var portlet = $('#' + NotificationPopover.popupId).parents('.uiNotificationPopoverToolbarPortlet:first');
        var badge =portlet.find('span.badgeDefault:first');
        var current = parseInt(badge.text().trim());
        badge.text((current + 1) + "").show();
        //
        portlet.find('.actionMark:first').show();
        
      }
  };
  
  NotificationPopover.init();
  window.NotificationPopover = NotificationPopover;
  return NotificationPopover;
})(jQuery);