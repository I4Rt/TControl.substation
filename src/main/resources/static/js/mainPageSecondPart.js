function updateData(){
    fetch ("/getMapAndListArraysJSON", {
        method: "POST",
        headers: {
            'Content-Type': 'application/json',
        },
        body: null,
    })
        .then((response) => {
            response.text().then(function (data){
                currentData = JSON.parse(data);
                console.log(currentData);

                redraw();
            })

        })
        .catch((error) => {
            console.error('Error:', error);
        });
}





function updateWeather(){
    fetch("/getWeather", {
        method: "POST",
        headers: {
            'Content-Type': 'application/json',
        },
        body: null,
    })
        .then((response) => {
            response.text().then(function (data){
                $(".weatherBar").html(data)
            })

        })
        .catch((error) => {
            console.error('Error:', error);
        });
}
setInterval(function(){
    updateData();
    updateAlertsFetch();
    updateWeather();
}, 3000);

function beep() {
    var snd = new Audio("data:audio/wav;base64,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qJrx4KOQjahZxWbcZgztj2c49nKmkId44S71j0c8eV9yDK6uPRzx5X18eDvjvQ6yKo9ZSS6l//8elePK/Lf//IInrOF/FvDoADYAGBMGb7FtErm5MXMlmPAJQVgWta7Zx2go+8xJ0UiCb8LHHdftWyLJE0QIAIsI+UbXu67dZMjmgDGCGl1H+vpF4NSDckSIkk7Vd+sxEhBQMRU8j/12UIRhzSaUdQ+rQU5kGeFxm+hb1oh6pWWmv3uvmReDl0UnvtapVaIzo1jZbf/pD6ElLqSX+rUmOQNpJFa/r+sa4e/pBlAABoAAAAA3CUgShLdGIxsY7AUABPRrgCABdDuQ5GC7DqPQCgbbJUAoRSUj+NIEig0YfyWUho1VBBBA//uQZB4ABZx5zfMakeAAAAmwAAAAF5F3P0w9GtAAACfAAAAAwLhMDmAYWMgVEG1U0FIGCBgXBXAtfMH10000EEEEEECUBYln03TTTdNBDZopopYvrTTdNa325mImNg3TTPV9q3pmY0xoO6bv3r00y+IDGid/9aaaZTGMuj9mpu9Mpio1dXrr5HERTZSmqU36A3CumzN/9Robv/Xx4v9ijkSRSNLQhAWumap82WRSBUqXStV/YcS+XVLnSS+WLDroqArFkMEsAS+eWmrUzrO0oEmE40RlMZ5+ODIkAyKAGUwZ3mVKmcamcJnMW26MRPgUw6j+LkhyHGVGYjSUUKNpuJUQoOIAyDvEyG8S5yfK6dhZc0Tx1KI/gviKL6qvvFs1+bWtaz58uUNnryq6kt5RzOCkPWlVqVX2a/EEBUdU1KrXLf40GoiiFXK///qpoiDXrOgqDR38JB0bw7SoL+ZB9o1RCkQjQ2CBYZKd/+VJxZRRZlqSkKiws0WFxUyCwsKiMy7hUVFhIaCrNQsKkTIsLivwKKigsj8XYlwt/WKi2N4d//uQRCSAAjURNIHpMZBGYiaQPSYyAAABLAAAAAAAACWAAAAApUF/Mg+0aohSIRobBAsMlO//Kk4soosy1JSFRYWaLC4qZBYWFRGZdwqKiwkNBVmoWFSJkWFxX4FFRQWR+LsS4W/rFRb/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////VEFHAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAU291bmRib3kuZGUAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAMjAwNGh0dHA6Ly93d3cuc291bmRib3kuZGUAAAAAAAAAACU=");
    snd.play();
}
function redraw(){
    $(".maxSize").empty();


    let dataToDisplay = currentData[1];
    for(let elementId = 0; elementId < dataToDisplay.length; elementId+=1) {

        $(".maxSize").append("<a class=\"marker mapMarker " + dataToDisplay[elementId]["temperatureClass"] + "\" style=\"position: absolute; top:" + dataToDisplay[elementId]["mapY"] + "px; left: " + dataToDisplay[elementId]["mapX"] + "px\" title=\"" + dataToDisplay[elementId]["name"] + "\"  name=\"" + dataToDisplay[elementId]["id"] + "\" >");
    }

    $(".areaList").empty();
    let dataToList = currentData[0];
    for(let elementId = 0; elementId < dataToList.length; elementId+=1) {
        $(".areaList").append("<a class=\"myRow areaInfo\" href=\"/area?id=" + dataToList[elementId]["id"] + "\"><text>"+dataToList[elementId]["name"]+"</text><div class=\"marker " + dataToList[elementId]["temperatureClass"] + "\" name=\"" + dataToList[elementId]["id"] + "\"></div></a>");
    }
    activateMarkers();
}


let needDelete = false;

$(".main").click(function (){
    $(".system").addClass("hidden");
    $(".directions").addClass("hidden");
    $(".user").addClass("hidden");
});

$("#sysActions").click(function (){
    $(".directions").addClass("hidden");
    $(".user").addClass("hidden");
    $(".system").toggleClass("hidden");
});
$(".system").click(function (){
    $(".system").toggleClass("hidden");
});

$("#directActions").click(function (){
    $(".system").addClass("hidden");
    $(".user").addClass("hidden");
    $(".directions").toggleClass("hidden");
});
$(".directions").click(function (){
    $(".directions").toggleClass("hidden");
});

$("#userActions").click(function (){
    $(".system").addClass("hidden");
    $(".directions").addClass("hidden");
    $(".user").toggleClass("hidden");
});
$(".user").click(function (){
    $(".user").toggleClass("hidden");
});


let offset = $(".mapImage").offset();

let preparedToAddElementId = null;

let readyToAddNew = 0;


$(document).click(function (e){
    console.log("document click")
    console.log(readyToAddNew)
    if (readyToAddNew == 1){
        if(e.pageX > offset.left && e.pageX < offset.left + 800 && e.pageY > offset.top && e.pageY < offset.top + 600){
            newX = e.pageX - offset.left - 11;
            newY = e.pageY - offset.top - 11
            fetch("/changeCoordinates", {
                method: "POST",
                headers: {
                    'Content-Type': 'application/json',
                },
                body: "{\"x\": "+newX+", \"y\": "+newY+", \"id\":"+preparedToAddElementId+"}",
            })
                .then((response) => {
                    response.text().then(function (){
                        updateData();
                    });
                })
                .catch((error) => {
                    console.error('Error:', error);
                });
            preparedToAddElementId = null;
        }

    }
    readyToAddNew -= 1;
    if(readyToAddNew < 0){
        readyToAddNew = 0;
    }
    console.log(readyToAddNew);
});

$("#newMarker").click(function (){

    console.log("Ready to add")
    $(".chooseObjectWindow").removeClass("hidden");
    $(".chooseObjectWindow>.areaInfo").each(function (){
        $(this).click(function (){
            preparedToAddElementId = $(this).attr('name');
            console.log("magic: "+ preparedToAddElementId);
            $(".chooseObjectWindow").addClass("hidden");
            readyToAddNew = 2;
        });
    });
});

$("#upload").click(function (){
    console.log('showed');
    $(".uploadWindow").removeClass("hidden");
});

$(".cross").click(function (){
    console.log('closed');
    $(".uploadWindow").addClass("hidden");
});

$("#closeObjectWindow").click(function (){
    $(".chooseObjectWindow").addClass("hidden")
})

$("#refreshConnections").click(function (){
    fetch ("/resetConnections", {
        method: "GET",
        headers: {
            'Content-Type': 'application/json',
        },
        body: null,
    })
        .then((response) => {
            response.text().then(function (data){
                alert(data)
            })
        })
        .catch((error) => {
            console.error('Error:', error);
        });
});
function activateMarkers(){
    $('.maxSize>.mapMarker').each(function (){
        $(this).click(function () {
            console.log('clicked marker');
            if(needDelete){
                fetch("/dropMapCoordinates?id=" + parseInt($(this).attr('name')), {
                    method: "PUT",
                    headers: {
                        'Content-Type': 'application/json',
                    },
                    body: null,
                })
                    .catch((error) => {
                        console.error('Error:', error);
                    });

                $(this).remove();
                needDelete = false
            }
            else{
                window.location.replace("area?id=" + parseInt($(this).attr('name')));
            }
        })
    })
}

$('#removeMarker').click(function (){
    needDelete = true;
})

function updateAlertsFetch(){
    fetch ("/getAlerts", {
        method: "GET",
        headers: {
            'Content-Type': 'application/json',
        },
        body: null,
    })
        .then((response) => {
            response.text().then(function (data){
                console.log('caught');
                let insideData = JSON.parse(data);
                if(firstTIError != insideData["firstTIError"]){
                    needShowFirstTIError = true;
                }
                else {
                    needShowFirstTIError = false
                }
                if(secondTIError != insideData["secondTIError"]){
                    needShowSecondTIError = true;
                }
                else {
                    needShowSecondTIError = false
                }
                if(thirdTIError != insideData["thirdTIError"]){
                    needShowThirdTIError = true;
                }
                else {
                    needShowThirdTIError = false
                }
                if(fourthTIError != insideData["fourthTIError"]){
                    needShowFourthTIError = true;
                }
                else {
                    needShowFourthTIError = false
                }
                if(weatherStationError != insideData["weatherStationError"]){
                    needShowWeatherStationError = true;
                }
                else {
                    needShowWeatherStationError = false
                }
                needBeep = insideData["beep"];
                firstTIError = insideData["firstTIError"];
                secondTIError = insideData["secondTIError"];
                thirdTIError = insideData["thirdTIError"];
                fourthTIError = insideData["fourthTIError"];
                weatherStationError = insideData["weatherStationError"];
                updateAlerts();
                if(needBeep){
                    beep();
                }
            })

        })
        .catch((error) => {
            console.error('Error:', error);
        });
}
function updateAlerts(){
    if(needShowFirstTIError && firstTIError){
        $("#ti1_ok").addClass('hidden');
        $("#ti1_error").removeClass('hidden');
    }
    if(needShowSecondTIError && secondTIError){
        $("#ti2_ok").addClass('hidden');
        $("#ti2_error").removeClass('hidden');
    }
    if(needShowThirdTIError && thirdTIError){
        $("#ti3_ok").addClass('hidden');
        $("#ti3_error").removeClass('hidden');
    }
    if(needShowFourthTIError && fourthTIError){
        $("#ti4_ok").addClass('hidden');
        $("#ti4_error").removeClass('hidden');
    }
    if(needShowWeatherStationError && weatherStationError){
        $("#ws_ok").addClass('hidden');
        $("#ws_error").removeClass('hidden');
    }
    if(needShowFirstTIError && !firstTIError){
        $("#ti1_error").addClass('hidden');
        $("#ti1_ok").removeClass('hidden');
    }
    if(needShowSecondTIError && !secondTIError){
        $("#ti2_error").addClass('hidden');
        $("#ti2_ok").removeClass('hidden');
    }
    if(needShowThirdTIError && !thirdTIError){
        $("#ti3_error").addClass('hidden');
        $("#ti3_ok").removeClass('hidden');
    }
    if(needShowFourthTIError && !fourthTIError){
        $("#ti4_error").addClass('hidden');
        $("#ti4_ok").removeClass('hidden');
    }
    if(needShowWeatherStationError && !weatherStationError){
        $("#ws_error").addClass('hidden');
        $("#ws_ok").removeClass('hidden');
    }
}
$(".messageCross").each(function (){
    $(this).click(function (){
        $(this).parent().addClass("hidden");
    });
});
$(document).ready(
    activateMarkers()
)