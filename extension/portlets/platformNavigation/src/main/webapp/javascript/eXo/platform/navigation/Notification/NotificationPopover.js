(function($){
  var NotificationPopover = {
      popupId : 'NotificationPopup',
      maxItem : 13,
      popupItem : null,
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
      },
      appendMessage : function(message) {
        var newItem = $('<li></li>').text(message);
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
        var badge = $('#' + NotificationPopover.popupId).parents('.uiNotificationPopoverToolbarPortlet:first').find('span.badgeDefault:first');
        console.log(badge);
        
        var current = parseInt(badge.text().trim());
        badge.text((current + 1) + "");
      }
  };
  
  NotificationPopover.init();
  window.NotificationPopover = NotificationPopover;
  return NotificationPopover;
})(jQuery);