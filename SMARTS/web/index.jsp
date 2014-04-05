<%-- 
    Document   : index
    Created on : 24.6.2013, 21:58:01
    Author     : Palpatine
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <link rel="stylesheet" href="css/result.css" />
        <script src="js/jquery-1.9.1.min.js"></script>
        <script>
            var progress;
            var smarts;
            var json;
            var numOfRecords = 50; //Const
            var timeStamp = (new Date()).getTime();
            
             $(function() {
                var queryObj = JSON.parse(decodeURIComponent((getCookie("queryObject"))));
                
                smarts = queryObj.smarts;
                json = queryObj.json;
                
                ask();
                
            });
            
            function getCookie(cname){
                var name = cname + "=";
                var ca = document.cookie.split(';');
                for(var i=0; i<ca.length; i++) 
                  {
                  var c = ca[i].trim();
                  if (c.indexOf(name)==0) return c.substring(name.length,c.length);
                  }
                return "";
            }
            
            function ask(){
                
                progress = true;
                $("#ask").text("Load " + numOfRecords + " more");
                $("#ask").css("display", "none");
                $("#loading").css("display", "block");
                
                $.ajax({
                    type: "GET",
                    data: "smarts=" + encodeURIComponent(smarts) + "&json=" + encodeURIComponent(json) + "&timeStamp=" + timeStamp + "&numOfRecords=" + numOfRecords,
                    url: "smarts",
                }).done(function(msg) {
                    progress = false;
                    var obj = JSON.parse(msg);
                    if(obj.length < numOfRecords){
                        $("#ask").text("All results loaded");
                        $('#ask').prop('disabled', true);
                    }
                    $.each(obj, function(index, value){
                       $("#answer").append("<tr><td>" + value.valid_number + "</td><td>" + value.id + "</td><td><a href='" + value.link + "'>" + value.smiles + "</td></tr>");
                    });
                    $("#ask").css("display", "block");
                    $("#loading").css("display", "none");
                });
                askProgress();
            }
            
            function askProgress(){
                $.ajax({
                    type: "GET",
                    url: "progress",
                }).done(function(msg) {
                    $("#progress").html(msg);
                });
                if(progress){
                    setTimeout(askProgress,500);
                }
            }
        </script>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JSP Page</title>
    </head>
    <body>
        <div id="progress"></div>
        <div class="TableDiv">
        <table id="answer">
            <tr>
                <td></td>
                <td>ID</td>
                <td>SMILES</td>
            <tr>
        </table>
        </div>
        <button id="ask" onclick="ask()" class="myButton"></button>
        <div id="loading"/>
    </body>
</html>
