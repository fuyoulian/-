package com.example.myapplication

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.accessibilityservice.GestureDescription.StrokeDescription
import android.content.res.Resources
import android.graphics.Path
import android.graphics.Point
import android.graphics.Rect
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Toast


class MyAccessibilityService : AccessibilityService() {

    private var isSwipe = false
    private var swipeCount = 0

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d("ffyl", "has connect")
    }

    override fun onInterrupt() {}

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        val rootNode = rootInActiveWindow
        if (rootNode == null) {
            Log.d("ffyl", "rootNode is null")
            return
        }
        traverseNode(rootNode)
    }

    private fun traverseNode(node: AccessibilityNodeInfo) {
        if (node.childCount > 0) {
            if (node.packageName == "com.p1.mobile.putong" && node.viewIdResourceName == "com.p1.mobile.putong.core:id/like") {
                Log.d("ffyl", "info: ${node.viewIdResourceName}, Text: ${node.text.toString()}")
                clickNode(node)
            }
            for (i in 0 until node.childCount) {
                val childNode = node.getChild(i)
                if (childNode != null) {
                    traverseNode(childNode)
                }
            }
        } else {
            try {
                //跳过广告
                if (node.packageName == "com.viva.time_todo" && (node.text?.contains("跳过") == true) || node.viewIdResourceName == "close_btn") {
                    Log.d("ffyl", "info: ${node.viewIdResourceName}, Text: ${node.text.toString()}")
                    clickNode(node)
                }
                //探探右滑
                if (node.packageName == "com.p1.mobile.putong" && node.viewIdResourceName == "com.p1.mobile.putong.core:id/like") {
                    swipeCount++
                    if (swipeCount <= 20) {
                        Thread.sleep(2000)
                        clickNode(node)
                    } else {
                        Toast.makeText(applicationContext, "已经喜欢20个", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
                if (node.packageName == "com.p1.mobile.putong"
                    && (node.viewIdResourceName == "com.p1.mobile.putong.core:id/enter_room_button" || node.text?.contains("闪聊匹配") == true)) {
                    if (!isSwipe) {
                        Thread.sleep(2000)
                        performLeftSwipe()
                    }
                }
            } catch (e: Exception) {
                Log.e("ffyl", e.message.toString())
            }

        }
    }

    private fun clickNode(node: AccessibilityNodeInfo) {
        val bounds = Rect()
        node.getBoundsInScreen(bounds)
        val clickPath = Path()
        clickPath.moveTo(bounds.exactCenterX(), bounds.exactCenterY())
        val gestureBuilder = GestureDescription.Builder()
        gestureBuilder.addStroke(StrokeDescription(clickPath, 0, 100))
        val gestureDescription = gestureBuilder.build()
        dispatchGesture(gestureDescription, null, null)
    }

    private fun performLeftSwipe() {
        isSwipe = true
        val screenWidth = Resources.getSystem().displayMetrics.widthPixels
        val screenHeight = Resources.getSystem().displayMetrics.heightPixels
        val x = screenWidth / 2
        val y = screenHeight / 2
        val gestureBuilder = GestureDescription.Builder()
        val startPoint = Point(x, y)
        val endPoint = Point(x - 500, y)
        val gesturePath = Path()
        gesturePath.moveTo(startPoint.x.toFloat(), startPoint.y.toFloat())
        gesturePath.lineTo(endPoint.x.toFloat(), endPoint.y.toFloat())
        gestureBuilder.addStroke(GestureDescription.StrokeDescription(gesturePath, 0, 500))
        val gestureResultCallback = object : GestureResultCallback() {
            override fun onCompleted(gestureDescription: GestureDescription?) {
                super.onCompleted(gestureDescription)
                isSwipe = false
            }

            override fun onCancelled(gestureDescription: GestureDescription?) {
                super.onCancelled(gestureDescription)
            }
        }
        dispatchGesture(gestureBuilder.build(), gestureResultCallback, null)
    }
}