//Load the plugin.
app.LoadPlugin( "GoProController" );

//Called when application is started. 
function OnStart()
{ 
    app.EnableBackKey( false );
    app.SetOrientation( "Portrait" );
    
    //Create a layout with objects vertically centered. 
    lay = app.CreateLayout( "Linear", "FillXY, VCenter" );
 
    //Create a layout for the connect button and camera name
    connectLay = app.CreateLayout( "Linear", "Horizontal, FillX" );
    connectLay.SetMargins(0, 0, 0, 0.05);
    connectBtn = app.CreateButton( "[fa-camera] Connect", -1, -1, "FontAwesome" );
    connectBtn.SetOnTouch( connect );
    connectLay.AddChild( connectBtn );
    connectTxt = app.CreateText( "" );
    connectTxt.SetMargins(0.01, 0, 0, 0);
    connectLay.AddChild( connectTxt );
    lay.AddChild( connectLay );
    
    //Create a layout for the camera controls
    controlLay = app.CreateLayout( "Linear", "Horizontal, VCenter" );
    controlLay.SetMargins(0, 0, 0, 0.05);
    modeSpin = app.CreateSpinner( "Video,Photo,Burst,Timelapse", 0.35 );
    modeSpin.SetOnTouch( modeSpin_OnChange );
    controlLay.AddChild( modeSpin );
    startShutterBtn = app.CreateButton( "Start Shutter" );
    startShutterBtn.SetOnTouch( startShutter_OnTouch );
    controlLay.AddChild( startShutterBtn );
    stopShutterBtn = app.CreateButton( "Stop Shutter" );
    stopShutterBtn.SetOnTouch( stopShutter_OnTouch );
    controlLay.AddChild( stopShutterBtn );
    lay.AddChild( controlLay );
    
    //Create the list to show the camera status properties
    statusList = app.CreateList( "", 1.0, 0.425 );
    lay.AddChild( statusList );
    
    //Add layout to app. 
    app.AddLayout( lay ); 
    
    //Create plugin and set the callbacks
    gopro = app.CreateObject( "GoProController" );
    gopro.SetOnConnect( gopro_OnConnect );
    gopro.SetOnReady( gopro_OnReady );
    gopro.SetOnError( gopro_OnError );
    
    //Connect to GoPro on startup
    connect();
}

function OnBack() 
{              
    gopro.Disconnect(); 
    app.Exit();
}

function connect()
{
    app.ShowProgress( "Connecting to GroPro..." );
    
    //Try to connect to the GoPro using the default ip address
    gopro.Connect( "10.5.5.9" );
}

//Called when successfully connected to the GoPro
function gopro_OnConnect()
{
    app.HideProgress();
    
    if( !gopro.IsPowerOn() )
    {
        var ynd = app.CreateYesNoDialog( "Camera is turned off, turn on now?" );
        ynd.SetOnTouch( powerOn_YesNo );
    }
}

function powerOn_YesNo(answer)
{
    if( answer == "Yes" )
    {
        gopro.PowerOn();
    }
}

//Called when the GoPro is powered on ready to be used
function gopro_OnReady()
{
    app.ShowPopup( "GoPro Ready" );
    
    //Show the GoPro model at the top
    connectTxt.SetText( "GoPro: " + gopro.GetModel() );
    
    //Load the camera status and call gopro_OnStatusLoaded when ready
    gopro.LoadStatus( gopro_OnStatusLoaded );
}

//Called when failed to connect to GoPro or when disconnected
function gopro_OnError( error )
{
    if( error == "NotFound" )
    {
        app.HideProgress();
        
        var errMsg = "GoPro not found. " +
                     "Make sure you connect to the GoPro Wifi hotspot."
        
        app.ShowPopup( errMsg );
    }
    else if( error == "Disconnected" )
    {
        app.ShowPopup( "GoPro disconnected." );
    }
}

function gopro_OnStatusLoaded( status )
{
    if( statusList.GetList(",").length == 0 )
    {
        //Populate the list for the first time
        for( var item in status )
        {
            statusList.AddItem( item, status[item] );
        }
    }
    else
    {
        //Update the existing list items
        for( var item in status )
        {
            statusList.SetItem( item, item, status[item] );
        }    
    }
    
    //Make sure the Mode spinner is showing the current mode
    if( modeSpin.GetText() != status.CameraMode )
    {
        modeSpin.SelectItem( status.CameraMode );
    }
 
    //Load the status again in 2 seconds
    setTimeout(function() { 
            gopro.LoadStatus( gopro_OnStatusLoaded ); 
        }, 2000);
}

function startShutter_OnTouch() 
{ 
    gopro.StartShutter();
}

function stopShutter_OnTouch() 
{ 
    gopro.StopShutter();
}

function modeSpin_OnChange( item )
{
    var options = {
        CameraMode: item
    };
    
    gopro.SetOptions( options ); 
}
