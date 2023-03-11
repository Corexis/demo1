package com.example.demo1

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.ui.Messages
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.http.HttpMethod
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class PluginAction : DumbAwareAction() {

    override fun actionPerformed(event: AnActionEvent) {

        val project = event.getData(PlatformDataKeys.PROJECT)
        Messages.showMessageDialog(project, "Hello from Kotlin!", "Greeting", Messages.getInformationIcon())

        val ktorClient = HttpClient(CIO) {
            install(WebSockets) {
                this.pingInterval = 20_000
            }
        }

        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {

            try {
                ktorClient.webSocket(
                    method = HttpMethod.Get,
                    host = "socketsbay.com",
                    path = "/wss/v2/1/demo",
                ) {
                    while (true) {
                        val incoming = (incoming.receive() as Frame.Text).readText()
                        thisLogger().info("incoming: $incoming")
                    }
                }
            } catch (ex: Exception) {
                this.thisLogger().error(ex)
            }
        }

    }
}