package com.sleepfuriously.dollargame2

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Intent
import android.graphics.Point
import android.graphics.PointF
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.text.SpannableStringBuilder
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.SwitchCompat
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.animation.PathInterpolatorCompat
import com.sleepfuriously.dollargame2.model.Graph
import com.sleepfuriously.dollargame2.model.GraphNotConnectedException
import com.sleepfuriously.dollargame2.model.SetsOfIntsUtil
import com.sleepfuriously.dollargame2.view.*
import com.sleepfuriously.dollargame2.view.SubButtonsBtn.ButtonEventListener
import com.sleepfuriously.dollargame2.view.buttons.MovableNodeButton
import com.sleepfuriously.dollargame2.view.dialogs.NodeEditDialog
import kotlin.math.abs
import kotlin.math.roundToInt


/**
 * My 2nd try at implementing the Dollar game as discussed by Dr. Holly Kreiger
 * on the Numberphile channel.  This time in kotlin.
 *
 *      https://www.youtube.com/watch?v=U33dsEcKgeQ
 */
class MainActivity : AppCompatActivity(), View.OnTouchListener {

    //------------------------------
    //  widgets
    //------------------------------

    /** play area of the game */
    private lateinit var mPlayArea : PlayAreaFrameLayout

    /** holds all the nodes and their connections */
    private val mGraph = Graph<MovableNodeButton>(false)

    /**
     * This switch toggles between build and solve mode.
     * Solve is true (on), false means build (off).
     */
    private lateinit var mMainSwitch : SwitchCompat

    /** Textviews that spell out BUILD or SOLVE at the top of the screen */
    private lateinit var mBuildTv : TextView
    private lateinit var mSolveTv : TextView

    /** displays hints to keep the user going */
    private lateinit var mHintTv : TextView

    /**
     * Displays the connectivity of the Graph while building.
     * Displays the solved state while solving
     *
     * TAG:     will be TRUE iff the current graph is connected
     */
    private lateinit var mConnectedIV : ImageView

    /** TextViews that deal with genus. Only meaningful when graph is connected */
    private lateinit var mGenusLabelTv : TextView
    private lateinit var mGenusTv : TextView

    /** TextViews that display the current count of the nodes */
    private lateinit var mCountLabelTv : TextView
    private lateinit var mCountTv : TextView

    /** allows the user to quickly randomize all the nodes at once */
    private lateinit var mRandomizeAllButt : Button

    /** this is the view that moves between the nodes indicating a give or take */
    private lateinit var mGiveTakeDrawable : Drawable


    //------------------------------
    //  data
    //------------------------------

    /** Reflects the current mode of the app: build or solve */
    private var mBuildMode = true

    /** true => in the process of connecting two nodes */
    private var mConnecting = false

    /** The id of the starting node in a connection or when moving */
    private var mStartNodeId = -1

    /** Used to indicate that a give action is taking place */
    private var mGiving = false

    /** Used to indicate that a take action is occurring */
    private var mTaking = false

    /** the size of an animation dot in current screen coordinates */
    private var mDotDimensionWidth = 0
    private var mDotDimensionHeight = 0

    /** only TRUE during the give/take animation. UI events need to wait until this is FALSE */
    private var mAnimatingGiveTake = false


    //------------------------------
    //  functions
    //------------------------------

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.d(TAG, "onCreate()")

        // setup?
        if (isStartingFromUser()) {
            userInitiatedOnCreate()
        }
        else {
            // there's some data to loaded and processed
            processIntent()
        }
    }



    /**
     * Initialize the main switch widget that controls build/solve mode.
     * Works by side-effect.
     */
    private fun setupMainSwitch() {
        mMainSwitch = findViewById(R.id.main_switch)
        mMainSwitch.setOnCheckedChangeListener() { _, isChecked ->
            if (isChecked == !mBuildMode) {
                Log.e(TAG, "onCheckedChanged() is trying to changed the mode to what it already is!")
            }
            setMode(!isChecked)
        }
    }


    /**
     * Initializes the widgets that control the count display.
     * Works by side-effect.
     */
    private fun setupCountWidgets() {
        mCountLabelTv = findViewById(R.id.count_label_tv)
        mCountLabelTv.setOnClickListener {
            showSimpleDialog(R.string.count_dialog_title, R.string.count_dialog_msg)
        }

        mCountTv = findViewById(R.id.count_tv)
        mCountTv.setOnClickListener {
            showSimpleDialog(R.string.count_dialog_title, R.string.count_dialog_msg)
        }
    }


    /**
     * Initializes the widgets that display the genus info.
     * Works by side-effect
     */
    private fun setupGenusWidgets() {
        mGenusLabelTv = findViewById(R.id.genus_label_tv)
        mGenusLabelTv.setOnClickListener {
            showSimpleDialog(R.string.genus_dialog_title, R.string.genus_dialog_msg)
        }

        mGenusTv = findViewById(R.id.genus_tv)
        mGenusTv.setOnClickListener {
            showSimpleDialog(R.string.genus_dialog_title, R.string.genus_dialog_msg)
        }
    }

    /**
     * Initializes the play area.
     * Works by side-effect
     */
    private fun setupPlayArea() {
        mPlayArea = findViewById(R.id.play_area_fl)
        mPlayArea.setOnTouchListener(this)
    }

    override fun onTouch(v: View?, playAreaEvent: MotionEvent?): Boolean {
        Log.d(TAG, "onTouch()")

        if (v == null) {
            // something weird happened--abort
            Log.e(TAG, "View is null in onTouch()! Aborting")
            return false
        }

        if (playAreaEvent == null) {
            // something weird happened--abort
            Log.e(TAG, "playAreaEvent is null in onTouch()! Aborting")
            v.performClick()
            return false
        }

        if (!mBuildMode) {
            // In Solve Mode, let the click-listeners do their thing
            v.performClick()
            return false
        }

        // if we're in the middle of a connection, handle all events
        if (mConnecting) {
            if (playAreaEvent.action == MotionEvent.ACTION_UP) {
                // finish the UI action and reset to non-connecting state
                mConnecting = false
                Log.d(TAG, "mConnect set to $mConnecting")

                // reset the start button
                val startButton = mGraph.getNodeData(mStartNodeId)
                startButton?.setBackgroundColorResource(getButtonStateColor(startButton))
                startButton?.invalidate()

                setAllButtonsBuild()        // allows buttons to be moved again
                buildModeUI()
            }
        }

        else if (playAreaEvent.action == MotionEvent.ACTION_UP) {
            // build a new button
            val touchLoc = PointF(playAreaEvent.x, playAreaEvent.y)
            newButton(touchLoc)
        }

        // todo: should we call v.performClick() here?
        return true
    }


    /**
     * Initializes the widgets that show if the graph is connected or not.
     * Works by side-effect
     */
    private fun setupConnectedWidgets() {
        mConnectedIV = findViewById(R.id.connected_iv)
        mConnectedIV.setOnClickListener {
            val toastStr : String
            if (mBuildMode) {
                val connected = mConnectedIV.tag as Boolean
                toastStr = if (connected)
                                getString(R.string.connected_toast)
                           else
                                getString(R.string.not_connected_toast)
            }
            else {
                // in solve mode
                toastStr = if (isSolved())
                                getString(R.string.solved_toast)
                           else
                                getString(R.string.not_solved_toast)
            }

            Toast.makeText(this, toastStr, Toast.LENGTH_LONG).show()
        }
    }


    /**
     * Initializes the randomize button.  This puts dollar amounts in all the
     * currently displayed buttons appropriate to the difficulty setting.  If
     * the graph is not connected, will set the count to 0.
     * Works by side-effect.
     */
    private fun setupRandomizeButton() {
        mRandomizeAllButt = findViewById(R.id.random_all_butt)
        mRandomizeAllButt.isEnabled = (mGraph.numNodes() > 0)

        mRandomizeAllButt.setOnClickListener {
            randomizeAllNodes()
        }
    }


    /**
     * Initializes all the main widgets for the game.
     * Works by side-effects.
     */
    private fun setupWidgets() {
        setupMainSwitch()
        setupCountWidgets()
        setupGenusWidgets()

        mBuildTv = findViewById(R.id.build_tv)
        mSolveTv = findViewById(R.id.solve_tv)
        mHintTv = findViewById(R.id.bottom_hint_tv)

        setupPlayArea()
        setupConnectedWidgets()
        setupRandomizeButton()

        // go ahead and crash if this can't be found
        mGiveTakeDrawable = AppCompatResources.getDrawable(this, R.drawable.circle_black_solid_small)!!

        // need to figure out the size of the animation dots for later
        val drawable = ContextCompat.getDrawable(this, R.drawable.circle_black_solid_small)
        mDotDimensionWidth = drawable!!.intrinsicWidth
        mDotDimensionHeight = drawable.intrinsicHeight

        // not animating currently; we've just started!
        mAnimatingGiveTake = false
    }


    /**
     * onCreate() if user initiated the Activity.  Does all the initializations
     * for the user to start the program from scratch.
     */
    private fun userInitiatedOnCreate() {
        Log.d(TAG, "userInitiatedOnCreate()")

        mBuildMode = true       // start in build mode
        mConnecting = false
        Log.d(TAG, "mConnect set to $mConnecting")
        setupToolbar()
        setupWidgets()
    }


    /**
     * This is the initialization portion of the program for when it has been
     * started via a notification.  Takes the user straight to the solve mode
     * and will have a puzzle laid out for him.
     */
    private fun notificationInitiatedOnCreate() {
        Log.d(TAG, "notificationInitiatedOnCreate()")

        mBuildMode = false
        mConnecting = false
        setupToolbar()
        setupWidgets()
    }


    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume()")

        // turn off status and navigation bar (top and bottom)
        fullScreenStickyImmersive()

        // handles refreshing UI
        refreshPrefs()
    }


    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        Log.d(TAG, "onNewIntent()")
        if (isStartingFromUser()) {
            Log.d(TAG, "detected a user initiated start from onNewIntent()--that's weird! aborting.")
            return
        }

        processIntent()
    }


    /**
     * This is overridden so that I can control when touch events are handled
     * and when they are all ignored.
     */
    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        Log.d(TAG, "displatchTouchEvent()")

        if (mAnimatingGiveTake) {
            return true     // consume all touch events during animation
        }
        return super.dispatchTouchEvent(ev)
    }


    private fun setupToolbar() {
        // use my toolbar instead of the default
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
    }


    /**
     * Tells if the Android UI components of status bar and navbar are
     * currently displayed.
     *
     * @return  True - yes, they are displayed
     */
    private fun isAndroidUiDisplaying() : Boolean {
        // Toggle Android UI elements.  This is complicated!
        // I have to compare the actual screen size (getMetrics)
        // with the size returned by getSize.  If they are different,
        // then the navbar and stuff are displayed.
        val metrics = DisplayMetrics()
        windowManager.defaultDisplay?.getMetrics(metrics)

        val p = Point()
        windowManager.defaultDisplay.getSize(p)

        var androidUiDisplaying = (metrics.heightPixels == p.y)
        if (androidUiDisplaying) {
            // check other axis (some devices keep the bars on the ends in landscape mode)
            androidUiDisplaying = (metrics.widthPixels == p.x)
        }

        return androidUiDisplaying
    }


    /**
     * Sets the display to fullscreen sticky immersive mode.
     *
     * @see  <a href="https://developer.android.com/training/system-ui/immersive#Options">google docs</a>
     */
    private fun fullScreenStickyImmersive() {

        // todo: fix these deprecated vars
        val decorView = window.decorView
        var uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        uiOptions = uiOptions or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        decorView.systemUiVisibility = uiOptions
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        if (mBuildMode) {
            menuInflater.inflate(R.menu.build_menu, menu)
        }
        else {
            menuInflater.inflate(R.menu.solve_menu, menu)
        }

        // nice simple font for the menu
        val tf = FontCache.get("fonts/roboto_med.ttf", this)

        // and change the fonts for the menu
        for (i in 0 until menu!!.size()) {
            val menuItem = menu.getItem(i)
            val ssb = SpannableStringBuilder(menuItem.title)

            val span = MyTypefaceSpan(tf)
            ssb.setSpan(span, 0, ssb.length, SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE)
            menuItem.title = ssb
        }

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.build_about -> doAbout()
            R.id.build_help -> Log.d(TAG, "menu option: help")
            R.id.build_load -> Log.d(TAG, "menu option: load")
            R.id.build_share -> Log.d(TAG, "menu option: share")
            R.id.build_settings -> doOptions()
            R.id.build_exit -> finish()
            else -> Toast.makeText(this, "unknown menu selection!", Toast.LENGTH_LONG).show()
        }
        return true
    }


    /**
     * Determines if this Activity was started by the user or by some other
     * notification (which implies that the Intent will have some important
     * data!).
     *
     * @return  True - user started this Activity. Use normal startup.
     *          False - Activity was started from a notification Intent.
     */
    private fun isStartingFromUser() : Boolean {
        val appLinkAction = intent.action
        assert(appLinkAction != null)
        return appLinkAction.equals(Intent.ACTION_MAIN)
    }


    /**
     * Does the work of processing the Intent that started this Activity.
     * The Intent holds any info that was sent to this Activity.
     *
     * This is primarily called after it's determined that this Activity was
     * NOT started by the user (and by elimination was called by a notification
     * Intent).  All the necessary data will be grabbed from the Intent here.
     *
     * Called by {@link #onCreate(Bundle)} and {@link #onNewIntent(Intent)}.
     *
     * side effects:
     *  todo
     *
     */
    private fun processIntent() {
        val rawDataStr = intent.dataString
        val appLinkAction = intent.action
        val appLinkData = intent.data

        if (appLinkData == null) {
            Log.e(TAG,"no data found in the Intent in processIntent()!")
            return
        }

        val host = appLinkData.host
        val scheme = appLinkData.scheme
        val queryParamNames = appLinkData.queryParameterNames
        val lastPathSegment = appLinkData.lastPathSegment

        // todo process data sent
    }


    /**
     * Changes the mode of all the buttons (nodes) to build mode (MOVABLE)
     */
    private fun setAllButtonsBuild() {
        Log.d(TAG, "setAllButtonBuild()")
        mGraph.getAllNodeData().forEach { button ->
            button.mode = MovableNodeButton.Modes.MOVABLE
        }
    }

    /**
     * Changes the nodes to Solve mode (EXPANDABLE)
     */
    private fun setAllButtonsSolve() {
        Log.d(TAG, "setAllButtonSolve()")
        mGraph.getAllNodeData().forEach { button ->
            button.mode = MovableNodeButton.Modes.EXPANDABLE
        }
    }


    /**
     * Changes all the nodes to NOT movable (CLICKS_ONLY) mode.  Used when
     * making a connection.
     */
    private fun setAllButtonsConnecting() {
        Log.d(TAG, "setAllButtonConnecting()")
        mGraph.getAllNodeData().forEach { button ->
            button.mode = MovableNodeButton.Modes.CLICKS_ONLY
        }
    }


    private fun showSimpleDialog(titleId : Int, msgId : Int) {
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setCancelable(false)
            .setTitle(titleId)
            .setMessage(msgId)
            .setNeutralButton(android.R.string.ok) { dialog, _ ->
                dialog.cancel()
            }

        dialogBuilder.create().show()
    }


    /**
     * Performs the logical operations for the new mode.
     * Also makes sure that the appropriate UI changes take place.
     *
     * Note that this doesn't take the CLICK_ONLY mode of the buttons into account.  That
     * is considered to be a sub-mode of BUILD mode.
     *
     * side effects:
     *      mModes      Changed to newMode
     *
     * @param buildMode     The new mode. True means Build mode; false is Solve.
     */
    private fun setMode(buildMode : Boolean) {
        Log.d(TAG, "setMode(buildMode = $buildMode")

        if (mBuildMode == buildMode) {
            Log.e(TAG, "setMode() is trying to change to the same mode! (mMode = $mBuildMode)")
            return
        }

        mBuildMode = buildMode

        // do the ui
        if (mBuildMode) {
            setAllButtonsBuild()
            buildModeUI()
        }
        else {
            setAllButtonsSolve()
            solveModeUI()
        }
    }


    /**
     * Does al the UI for the solve mode
     */
    private fun solveModeUI() {
        Log.d(TAG, "solveModeUI()")

        // only change the switch if NOT checked (in Build mode)
        if (!mMainSwitch.isChecked) {
            mMainSwitch.isChecked = true
        }

        // reset in case we're in the middle of a connect/disconnect
        if (mConnecting) {
            mConnecting = false
        }

        // make sure all the nodes are in the right color (it's possible that the switch
        // to solve-mode was in the middle of a connection)
        val nodes = mGraph.getAllNodeData()
        nodes.forEach { node ->
            node.setBackgroundColorResource(R.color.button_bg_color_build_normal)
            node.invalidate()
        }

        mBuildTv.setTextColor(resources.getColor(R.color.textcolor_ghosted))
        mSolveTv.setTextColor(resources.getColor(R.color.textcolor_on))

        // convert this widget to display the solvable state
        if (isSolved()) {
            mConnectedIV.setImageResource(R.drawable.ic_solved)
        }
        else {
            mConnectedIV.setImageResource(R.drawable.ic_unsolved)
        }

        mHintTv.setText(R.string.solve_hint)

        mRandomizeAllButt.visibility = View.GONE
    }


    /**
     * Does all the UI for changing to build mode.
     */
    private fun buildModeUI() {
        Log.d(TAG, "buildModeUI()")

        // only change the switch if it IS checked (in Solve mode)
        if (mMainSwitch.isChecked) {
            mMainSwitch.isChecked = false
        }

        mBuildTv.setTextColor(resources.getColor(R.color.textcolor_on))
        mSolveTv.setTextColor(resources.getColor(R.color.textcolor_ghosted))

        if (mGraph.isConnected()) {
            mConnectedIV.setImageResource(R.drawable.ic_connected)
        }
        else {
            mConnectedIV.setImageResource(R.drawable.ic_not_connected)
        }

        mHintTv.setText(R.string.build_hint)

        mRandomizeAllButt.visibility = View.VISIBLE
        mRandomizeAllButt.isEnabled = (mGraph.numNodes() > 0)
    }


    /**
     * Does the UI for making a connection.
     * Should ONLY be used while in Build mode.
     */
    private fun connectUI() {
        mHintTv.setText(R.string.connect_hint)
        mRandomizeAllButt.isEnabled = false
    }


    /**
     * Does all the genus UI.  If the graph is NOT connected, the genus doesn't make sense,
     * so the UI will reflect it.
     *
     * preconditions
     *      - genus widgets are initialized
     *      - mGraph contains all the correct info about the graph
     */
    private fun setGenusUI() {
        try {
            mGenusTv.text = mGraph.getGenus().toString()
        }
        catch (e : GraphNotConnectedException) {
            // this is not really an error--just a convenient way to see that the
            // graph is not connected
            mGenusTv.setText(R.string.not_applicable)
        }
    }

    private fun hideGenusUI() {
        mGenusTv.visibility = View.GONE
        mGenusLabelTv.visibility = View.GONE
    }

    private fun showGenusUI() {
        mGenusTv.visibility = View.VISIBLE
        mGenusLabelTv.visibility = View.VISIBLE
    }


    /**
     * Does all the UI for displaying the current count.  The count is simply the
     * sum of all the dollar amounts in all the nodes.  This is displayed whether
     * or not the graph is connected.
     *
     * If there are no nodes, then the count doesn't make sense and "not applicable"
     * will display.
     */
    private fun setCountUI() {
        if (mGraph.numNodes() == 0) {
            mCountTv.setText(R.string.not_applicable)
        }
        else {
            var count = 0
            mGraph.getAllNodeData().forEach { node ->
                count += node.amount
            }
            mCountTv.text = count.toString()
        }
    }

    private fun hideCountUI() {
        mCountTv.visibility = View.GONE
        mCountLabelTv.visibility = View.GONE
    }

    private fun showCountUI() {
        mCountTv.visibility = View.VISIBLE
        mCountLabelTv.visibility = View.VISIBLE
    }


    /**
     * Adds a button to the given coords.  Should only be called
     * when in Build mode.
     *
     * side effects:
     *  mGraph      Will have this button added to it
     *
     *  UI          Will have a new button drawn at the given location
     *
     * @param   relativeToParentLoc The location to center the button around.  This'll probably
     *                              be where the user touched the screen.
     *                              NOTE: this uses RELATIVE COORDINATES (to the parent)!
     */
    private fun newButton(relativeToParentLoc : PointF) {
        Log.d(TAG, "newButton( $relativeToParentLoc )")

        val button = MovableNodeButton(this)
        button.layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                                                       ViewGroup.LayoutParams.WRAP_CONTENT)

        button.id = mGraph.generateUniqueNodeId()

        button.setXYCenter(relativeToParentLoc.x, relativeToParentLoc.y)
        button.setBackgroundColorResource(R.color.button_bg_color_build_disconnected)

        button.setButtonEventListener(object : ButtonEventListener {

            override fun onPopupButtonClicked(index: Int) {
                when (index) {
                    1 -> mTaking = true
                    2 -> mGiving = true
                    else -> {}  // nop
                }
            }

            override fun onExpand() {
                resetPopupButtons()
            }

            override fun onCollapse() {
                // nop -- needed to complete interface
            }

            override fun onCollapseFinished() {
                if (mTaking || mGiving) {
                    startGiveTake(button.id, button)
                }
            }
        })

        button.setOnMoveListener(object : MovableNodeButton.OnMoveListener {
            override fun movingTo(diffX: Float, diffY: Float) {
                continueMove(button, diffX, diffY)
            }

            override fun moveEnded(diffX: Float, diffY: Float) {
                continueMove(button, diffX, diffY)
            }

            override fun clicked() {
                if (mConnecting) {
                    // process the connection, remembering that this is the 2nd button
                    // of the connection and you can't connect to yourself.
                    val endId = button.id

                    if (endId == mStartNodeId) {
                        // can't connect to yourself!
                        button.setBackgroundColorResource(getButtonStateColor(button))
                        button.invalidate()
                    }
                    else {
                        if (mGraph.isAdjacent(mStartNodeId, endId)) {
                            // remove this connection
                            disconnectButtons(mStartNodeId, endId)
                        }
                        else {
                            // add this connection
                            connectButtons(mStartNodeId, endId)
                        }
                    }

                    mConnecting = false
                    setAllButtonsBuild()
                    buildModeUI()
                }

                else {
                    // only start a connection if there's a button to connect to!
                    if (mGraph.numNodes() > 1) {
                        startConnection(button)
                    }
                }
            }

            override fun longClicked() {
                showMoneyDialog(button)
            }
        })

        mPlayArea.addView(button)

        mGraph.addNode(button, button.id)
        resetConnectedUI()

        // turn on the randomize all buttons if it's off
        if (!mRandomizeAllButt.isEnabled) {
            mRandomizeAllButt.isEnabled = true
        }
    }


    private fun resetPopupButtons() {
        mGiving = false
        mTaking = false
    }


    /**
     * Begins the animation of a give or take.  This involves considerable
     * setup.  Once the animation is complete {@link #giveTakeAnimFinished(List, MovableNodeButton)}
     * is called via a listener.
     *
     * preconditions:
     *  mGiving and mTaking should be properly set.
     *
     * @param mainButtId    The graph id of the button doing the taking
     *
     * @param mainButton    The button that we're sending the money to
     *                      (the button that's taking the money).
     */
    private fun startGiveTake(mainButtId : Int, mainButton : MovableNodeButton) {
        Log.d(TAG, "startGiveTake() - mainButtId = $mainButtId")

        // sanity check
        if ((mGiving == false) && (mTaking == false)) {
            Log.e(TAG, "no action to do in startGiveTake()!")
            return
        }

        // set a flag to prevent an UI event during this animation
        mAnimatingGiveTake = true

        var xAdjust = 0
        var yAdjust = 0

        // noinspection unchecked
        val adjacentList = mGraph.getAllAdjacentTo(mainButtId)

        // create the little moving dots that will traverse the edges
        val dots = ArrayList<ImageView>()
        adjacentList.forEach { adjacentId ->
            val newDot = ImageView(this)
            newDot.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                                                            LinearLayout.LayoutParams.WRAP_CONTENT)
            newDot.setImageDrawable(mGiveTakeDrawable)

            val adjacentButt = mGraph.getNodeData(adjacentId)
            if (adjacentButt == null) {
                Log.e(TAG, "adjacentButt is null in startGiveTake()! Aborting!")
                return
            }
            newDot.tag = adjacentButt     // we'll need this data later

            // Set the initial location of this dot on the main button or the adjacent
            // button depending on whether this is a give or a take.
            // The locations of the dots must account for the dot size (of course)
            xAdjust = (mDotDimensionWidth / 2f).toInt()
            yAdjust = (mDotDimensionHeight / 2f).toInt()

            if (mTaking) {
                newDot.x = adjacentButt.centerX - xAdjust
                newDot.y = adjacentButt.centerY - yAdjust
            }
            else {
                newDot.x = mainButton.centerX - xAdjust
                newDot.y = mainButton.centerY - yAdjust
            }

            dots.add(newDot)
            mPlayArea.addView(newDot)
        }

        // animate the dots
        dots.forEachIndexed {i, dot ->
            val adjacentButt = dot.tag as MovableNodeButton

            val animator = dot.animate()
            animator.duration = TAKE_MILLIS

            // custom interpolator. Tool at https://matthewlein.com/tools/ceaser
            val interpolator = PathInterpolatorCompat.create(0.485f, 0.005f, 0.085f, 1f)
            animator.interpolator = interpolator

            if (mTaking) {
                animator
                    .translationX(mainButton.centerX - xAdjust)
                    .translationY(mainButton.centerY - yAdjust)
            }
            else {
                animator
                    .translationX(adjacentButt.centerX - xAdjust)
                    .translationY(adjacentButt.centerY - yAdjust)
            }

            if (i + 1 == dots.size) {
                // if this is the last one, set a listener to fire when the animation ends
                animator.setListener(object : Animator.AnimatorListener,
                                       ValueAnimator.AnimatorUpdateListener {

                        override fun onAnimationEnd(animation : Animator?) {
                            Log.d(TAG, "onAnimationEnd()")
                            giveTakeAnimFinished(dots, mainButton)
                        }

                        override fun onAnimationStart(animation: Animator?) {
                            Log.d(TAG, "onAnimationStart()")
                        }

                        override fun onAnimationCancel(animation: Animator?) { }

                        override fun onAnimationRepeat(animation: Animator?) {
                            Log.d(TAG, "onAnimationRepeat()")
                        }

                        override fun onAnimationUpdate(animation: ValueAnimator?) {
                            Log.d(TAG, "onAnimationUpdate()")
                        }
                    })
            }
        }

    }


    /**
     * Called once a give or take animation is complete.  This finishes
     * the UI and the logic of a give or a take.<br>
     *<br>
     * preconditions:<br>
     *  mGiving and mTaking are properly set (should have been checked at
     *  a higher level).
     *
     * @param animViews List of all the dot Views that were animating
     *
     * @param mainButton    The button that is the center of the animation.
     */
    private fun giveTakeAnimFinished(animViews : List<ImageView>, mainButton : MovableNodeButton) {
        Log.d(TAG, "giveTakeAnimFinished()")

        animViews.forEach { dot ->
            // update the dollar amount connected to this view
            val destButton = dot.tag as MovableNodeButton
            if (mGiving) {
                destButton.incrementAmount()
            }
            else {
                destButton.decrementAmount()
            }

            // remove from the play area
            mPlayArea.removeView(dot)
        }

        // update the main button
        val currAmount = mainButton.amount
        val changeAmount = animViews.size

        if (mGiving) {
            mainButton.amount = currAmount - changeAmount
        }
        else {
            mainButton.amount = currAmount + changeAmount
        }

        // re-check solved state
        if (isSolved()) {
            mConnectedIV.setImageResource(R.drawable.ic_solved)
        }
        else {
            mConnectedIV.setImageResource(R.drawable.ic_unsolved)
        }

        mAnimatingGiveTake = false
    }


    /**
     * Checks the current state of mGraph and determines if we're in a solved
     * state or not.  If any node has less than 0 dollars, then the puzzle
     * is not solved.
     *
     * preconditions:
     *      mGraph      Ready for inspection
     */
    private fun isSolved() : Boolean {
        var solved = true
        mGraph.getAllNodeData().forEach { node ->
            if (node.amount < 0) {
                solved = false
            }
        }
        return solved
    }


    /**
     * Throws up a dialog that allows the user to edit the money amount within a node.
     *
     * @param   button      The node/button in question
     */
    private fun showMoneyDialog(button : MovableNodeButton) {
        Log.d(TAG, "showMoneyDialog()")

        val dialog = NodeEditDialog()
        dialog.setOnNodeEditDialogDoneListener { cancelled, dollarAmount, delete ->
            if (cancelled) {
                return@setOnNodeEditDialogDoneListener  // do nothing
            }

            if (delete) {
                deleteNode(button)
                return@setOnNodeEditDialogDoneListener
            }

            // set the button to the dollar amount
            button.amount = dollarAmount
            setCountUI()
        }
        dialog.show(this, button.amount)
    }


    private fun deleteNode(nodeToDelete : MovableNodeButton) {
        Log.d(TAG, "deleteNode() - nodeToDelete ID = ${nodeToDelete.id}")

        val nodeId = mGraph.getNodeId(nodeToDelete)
        if (nodeId == null) {
            Log.e(TAG, "Can't find the id in deleteNode()!  aborting!!!")
            return
        }

        mPlayArea.removeView(nodeToDelete)
        mGraph.removeNode(nodeId)

        resetAllButtonStateColors()
        rebuildPlayAreaLines()
        resetConnectedUI()

        if (mGraph.numNodes() == 0) {
            mRandomizeAllButt.isEnabled = false     // turn off if no more buttons
        }
    }


    /**
     * Call this to execute the logic and the UI of a node button being moved.
     * This won't move the button (which can take care of itself), but will do
     * everything else.
     *
     * @param button    The node that is moved.
     *
     * @param diffX     The delta between the previous coords and the new coords.
     *                  In other words, the x-axis will move this many pixels.
     *
     * @param diffY     Similar for y-axis
     */
    private fun continueMove(button: MovableNodeButton, diffX : Float, diffY : Float) {
        Log.d(TAG, "continueMove() button ID = ${button.id}, diff = ($diffX, $diffY)")
        // todo: this would be more efficient--make updateLines() method work
//        PointF diffPoint = new PointF(diffX, diffY);
//        mPlayArea.updateLines(button.getCenter(), diffPoint);

        rebuildPlayAreaLines()
        mPlayArea.invalidate()
    }


    /**
     * Helper method that clears all the lines from the play area and reconstructs
     * them according to mGraph.
     */
    private fun rebuildPlayAreaLines() {
        Log.d(TAG, "rebuildPlayAreaLines()")

        mPlayArea.removeAllLines()

        mGraph.getAllEdges().forEach { edge ->
            val startButton = mGraph.getNodeData(edge.startNodeId)
            if (startButton == null) {
                Log.e(TAG, "cannot find startButton in rebuildPlayAreaLines(), aborting!")
                return
            }

            val endButton = mGraph.getNodeData(edge.endNodeId)
            if (endButton == null) {
                Log.e(TAG, "cannot find endButton in rebuildPlayAreaLines(), aborting!")
                return
            }

            mPlayArea.addLine(startButton.center, endButton.center)
        }
    }


    /**
     * Call this to initiate a node connection.
     *
     * @param button    The button that starts the connection.  This will
     *                  be highlighted.
     */
    private fun startConnection(button : MovableNodeButton) {
        Log.d(TAG, "startConnection() - button ID = ${button.id}")

        mConnecting = true
        mStartNodeId = button.id

        setAllButtonsConnecting()
        connectUI()

        button.setBackgroundColorResource(R.color.button_bg_color_build_connect)
        button.invalidate()
    }


    /**
     * Does the logic and graphics of removing the connection between two buttons.
     *
     * @param startButtonId     Id of the start button. This is the button that the
     *                          user first selected when making the dis-connection
     *                          and thus is currently highlighted.
     *
     * @param endButtonId   The id of the other button.
     */
    private fun disconnectButtons(startButtonId : Int, endButtonId : Int) {
        Log.d(TAG, "disconnectButtons() - start Id = $startButtonId, end Id = $endButtonId")

        // cannot disconnect yourself!
        if (startButtonId == endButtonId) {
            Log.w(TAG, "attempting to disconnect a button from itself--aborted.")
            return
        }

        // check to make sure these buttons actually ARE connected
        if (mGraph.isAdjacent(startButtonId, endButtonId) == false) {
            Log.e(TAG, "attempting to disconnected two nodes that are not connected! Aborting!");
            return;
        }

        val startButton = mGraph.getNodeData(startButtonId)
        val endButton = mGraph.getNodeData(endButtonId)
        if ((startButton == null) || (endButton == null)) {
            Log.e(TAG, "can't find the buttons to disconnect--aborting!")
            return
        }

        // remove from graph and play area
        mGraph.removeEdge(startButtonId, endButtonId)
        mPlayArea.removeLine(startButton.center, endButton.center)
        mPlayArea.invalidate()

        startButton.setBackgroundColorResource(getButtonStateColor(startButton))
        endButton.setBackgroundColorResource(getButtonStateColor(endButton))

        startButton.mode = MovableNodeButton.Modes.MOVABLE
        endButton.mode = MovableNodeButton.Modes.MOVABLE

        startButton.invalidate()
        endButton.invalidate()
        resetConnectedUI()
    }


    /**
     * Does the logic and graphics of connecting two buttons.  Presumes that they are NOT
     * already connected.  Use {@link Graph#isAdjacent(int, int)} to determine if
     * the two nodes/buttons are already connected.
     *
     * side effects:
     *      mGraph      Will reflect the new connection
     *
     * @param startButtonId     The beginning button (node). Should be highlighted.
     *
     * @param endButtonId       Destination button (node)
     */
    private fun connectButtons(startButtonId: Int, endButtonId: Int) {
        Log.d(TAG, "connectButtons() - start Id = $startButtonId, end Id = $endButtonId")
        // cannot connect to yourself!
        if (startButtonId == endButtonId) {
            Log.v(TAG, "Attempting to connect a button to itself--aborted.");
            return;
        }

        // check to make sure these buttons aren't already connected
        if (mGraph.isAdjacent(startButtonId, endButtonId)) {
            Log.e(TAG, "Attempting to connect nodes that are already connected! Aborting!");
            return;
        }

        setAllButtonsBuild()        // this can only happen during the build mode  todo: is this necessary?

        val startButton = mGraph.getNodeData(startButtonId)
        val endButton = mGraph.getNodeData(endButtonId)
        if ((startButton == null) || (endButton == null)) {
            Log.e(TAG, "can't find the buttons to connect--aborting!")
            return
        }

        // add this new line to the graph and play area
        mGraph.addEdge(startButtonId, endButtonId)
        mPlayArea.addLine(startButton.center, endButton.center)
        mPlayArea.invalidate()

        startButton.setBackgroundColorResource(getButtonStateColor(startButton))
        endButton.setBackgroundColorResource(getButtonStateColor(endButton))

        startButton.invalidate()
        endButton.invalidate()
        resetConnectedUI()
    }


    /**
     * Goes through each node/button and checks it's state, making sure
     * that it is displaying the correct color.
     */
    private fun resetAllButtonStateColors() {
        Log.d(TAG, "resetAllButtonStateColors()")

        mGraph.getAllNodeIds().forEach { nodeId ->
            val nodeButton = mGraph.getNodeData(nodeId)
            if (nodeButton == null) {
                Log.e(TAG, "can't find node/button in resetAllButtonStateColors() -- aborting!")
                return
            }
            val color = getButtonStateColor(nodeButton)

            nodeButton.setBackgroundColorResource(color)
            nodeButton.invalidate()
        }
    }


    /**
     * Figures out the appropriate color for this button based on its current state.
     */
    private fun getButtonStateColor(button : MovableNodeButton) : Int {
        // if the node is connected to any other node, then use the connected color
        val connectedNodes = mGraph.getAllAdjacentTo(button.id)
        if (connectedNodes.size > 0) {
            return R.color.button_bg_color_build_connected
        }
        else {
            return R.color.button_bg_color_build_disconnected
        }
    }


    /**
     * Sets the drawing in the connection ImageView according to the current
     * state of the graph.
     *
     * side effects:
     *  mConnectedIV    may have its source image changed
     *
     *  mMainSwitch     Will be enabled/disabled depending on the state of the Graph
     */
    private fun resetConnectedUI() {
        Log.d(TAG, "resetConnectedUI()")

        if (mGraph.isConnected()) {
            mConnectedIV.setImageResource(R.drawable.ic_connected)
            mConnectedIV.tag = true     // indicates that is IS displaying a connected graph
            mMainSwitch.isEnabled = true
        }
        else {
            mConnectedIV.setImageResource(R.drawable.ic_not_connected)
            mConnectedIV.tag = false     // displaying a NOT connected graph
            mMainSwitch.isEnabled = false
        }
        mConnectedIV.invalidate()

        setCountUI()
        setGenusUI()

        mRandomizeAllButt.visibility = View.VISIBLE
        mRandomizeAllButt.isEnabled = (mGraph.numNodes() > 0)
    }


    /**
     * Does the logic and UI of randomizing the contents of all the nodes.
     */
    private fun randomizeAllNodes() {
        Log.d(TAG, "randomizeAllNodes()")

        val nodeIds = mGraph.getAllNodeIds()
        val numNodes = nodeIds.size

        // get max and min values
        val ceiling = resources.getInteger(R.integer.MAX_DOLLAR_AMOUNT)
        val floor = resources.getInteger(R.integer.MIN_DOLLAR_AMOUNT)

        // use the settings to figure out what the sum of the nodes' dollar
        // amount should be
        var targetSum : Int = getCurrentDifficulty()
        try {
            targetSum += mGraph.getGenus()
        }
        catch (e : GraphNotConnectedException) {
            Log.v(TAG, "Randomizing nodes before graph is connected. No big deal.");
        }


        // THIS IS IT!!!
        val util = SetsOfIntsUtil()
        var randomNums = util.findRandomSetOfIntsWithGivenSum(targetSum, numNodes, floor, ceiling)

        if ((randomNums == null) || (randomNums.size == 0)) {
            Log.e(TAG, "Unable to create combinations, aborting!  (randomNums = $randomNums)")
            Toast.makeText(this, R.string.unable_to_generate_random_node_numbers, Toast.LENGTH_LONG).show()
            return
        }

        if (randomNums.size != numNodes) {
            Log.e(TAG, "wrong number of random numbers! (expected $numNodes, but got ${randomNums.size})\n    we'll probably crash real soon.")
            var errSum = 0
            randomNums.forEach { item ->
                errSum += item
            }
            Log.e(TAG, "    their sum is: $errSum")
            return
        }

//      start statistical analysis
//        displayDistribution(randomNums, floor, ceiling);
//      end stat analysis

        // go through all the nodes and assign them to the dollar amounts from our list
        nodeIds.forEachIndexed { i, id ->
            val node = mGraph.getNodeData(id)
            node!!.amount = randomNums[i]
        }

        setGenusUI()
        setCountUI()
    }


    /**
     * Displays a distribution graph of the numbers in the given array.
     *
     * The output will be a graph showing how many items (relative to each other)
     * are in each number.
     */
    private fun displayDistribution(array : Array<Int>, floor : Int, ceiling : Int) {

        // get stats of each number
        val statsArray = IntArray(ceiling - floor + 1)

        array.forEach {
            var currentRandomNum = it
            currentRandomNum -= floor       // normalize so that zero is the bottom
            statsArray[currentRandomNum]++
        }

        Log.d(TAG, "Analysis:")
        val builder = StringBuilder()
        for (i in statsArray.indices) {
            builder.append("${abs(i + floor)} ")
        }
        Log.d(TAG, builder.toString())


        builder.clear()
        statsArray.forEach { value ->
            builder.append("$value ")
        }
        Log.d(TAG, builder.toString())


        builder.clear()
        statsArray.forEach { v ->
            var currentQuantity = v.toFloat()               // a number in range [-5..5]
            currentQuantity += 5f                           // adjust to zero
            currentQuantity /= (statsArray.size).toFloat()
            currentQuantity *= 5f                           // range [0..5]
            val quantity = currentQuantity.roundToInt()

            when (quantity) {
                0, 1 -> builder.append(" ")
                2 -> builder.append(0x2581)
                3 -> builder.append(0x2583)
                4 -> builder.append(0x2585)
                5 -> builder.append(0x2588)
                else -> builder.append('E')                 // shouldn't happen
            }
            builder.append(" ")
        }

        // here's the stats!
        Log.d(TAG, builder.toString())
    }


    /**
     * Checks the shared prefs to find the current difficulty setting.
     *
     * @return  An int representing how many dollars should be adjusted
     *          above (or below if negative) the genus according to the
     *          current user's preference.
     */
    private fun getCurrentDifficulty() : Int {
        Log.d(TAG, "getCurrentDifficulty()")

        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val diffKey = getString(R.string.pref_gameplay_difficulty_key)
        val diffVal = prefs.getString(diffKey, null)

        if (diffVal == null) {
            Log.e(TAG, "could not find difficulty key in getCurrentDifficulty()!");

            // todo: this should be an error condition
            return 0
        }

        var retVal = 0
        when (diffVal) {
            "1" -> retVal = 2       // very easy
            "2" -> retVal = 1       // easy
            "3" -> retVal = 0       // challenging
            "4" -> retVal = -1      // not always possible
            else -> {
                Log.e(TAG, "unable to figure out diffVal in getCurrentDifficulty()!")
            }
        }

        return retVal
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d(TAG, "onActivityResult()")

        when (requestCode) {
            PREFS_ACTIVITY_ID -> refreshPrefs()
            else -> Log.e(TAG, "unknown requestCode of $requestCode in onActivityResult()")
        }
    }


    /**
     * Simple dialog that gives basic info about this app.
     */
    private fun doAbout() {
        startActivity(Intent(this, AboutActivity::class.java))
    }


    /**
     * Throws up the options dialog and all that entails.
     */
    private fun doOptions() {
        // using an Activity for prefs because I'm lazy and haven't setup the UI for
        // fragment. uh yeah.
        val itt = Intent(this, PrefsActivity::class.java)
        startActivityForResult(itt, PREFS_ACTIVITY_ID)
    }


    /**
     * Updates variables and resets UI according to a fresh loading of the preferences.
     *
     * preconditions:
     *      - All the preference globals are initialized.
     *      - All UI elements that can change via prefs are ready to roll.
     *
     * side effects:
     *      mHintTV - visibility changes
     */
    private fun refreshPrefs() {
        Log.d(TAG, "refreshPrefs()")

        val prefs = PreferenceManager.getDefaultSharedPreferences(this)

        // hints
        val showHint = prefs.getBoolean(resources.getString(R.string.pref_hints_cb_key), true)
        mHintTv.visibility = if (showHint) View.VISIBLE else View.GONE

        // difficulty
        // todo?

        // construct the UI according to current mode
        if (mBuildMode) {
            buildModeUI()
        }
        else {
            solveModeUI()
        }
    }


    //------------------------------
    //  constants
    //------------------------------

    companion object {
        const val TAG = "MainActivity"

        /** Identifies the PrefsActivity on onActivityResult() */
        const val PREFS_ACTIVITY_ID = 2

        /** number of milliseconds for a TAKE animation */
        const val TAKE_MILLIS = 300L
    }

}