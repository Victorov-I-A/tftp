import java.io.*
import java.lang.Exception
import java.net.*
import java.nio.file.Files


fun main(args: Array<String>) {
    val s = Server();
    s.start()
}

class Server {
    fun start() {
        val server = ServerSocket(9999)
        val client = server.accept()

        val input = ByteArray(Utils.PACKET_SIZE)
        client.getInputStream().read(input)
        val request = Utils.unpackRequest(input)
        if (request.first == Utils.Opcode.RRQ) readMode(request, client)
        if (request.first == Utils.Opcode.WRQ) writeMode(request, client)

    }

    private fun writeMode(request: Pair<Utils.Opcode, List<String>>, client: Socket) {
        client.soTimeout = Utils.SOCKET_TIMEOUT

        val dataOutputStream = client.getOutputStream()
        val dataInputStream = client.getInputStream()

        val fileContent = ByteArrayOutputStream()
        var currentBlock: Short = 0
        var readBytes: Int

        dataOutputStream.write(Utils.packACK(currentBlock))
        while (true) {
            val input = ByteArray(Utils.PACKET_SIZE)
            try {
                readBytes = dataInputStream.read(input)
            } catch (e: SocketException) {
                TODO("Not yet implemented")
            }
            when (input[1].toShort()) {
                Utils.Opcode.Error.code -> {
                    TODO("Not yet implemented")
                }
                Utils.Opcode.Data.code -> {
                    val packData = Utils.unpackData(input)
                    if (currentBlock == packData.first) {
                        fileContent.write(input.copyOfRange(4, readBytes))
                        if (readBytes != Utils.PACKET_SIZE) {
                            break
                        }
                        currentBlock++
                    }
                    dataOutputStream.write(Utils.packACK(currentBlock))
                }
                else -> {
                    TODO("Not yet implemented")
                }
            }
        }
        val fileName = request.second[0].split('/').last()
        File(fileName).writeBytes(fileContent.toByteArray())
        println("File $fileName downloaded successfully.")
    }

    private fun readMode(request: Pair<Utils.Opcode, List<String>>, client: Socket) {
        client.soTimeout = Utils.SOCKET_TIMEOUT
        val dataInputStream = DataInputStream(client.getInputStream())
        val dataOutputStream = DataOutputStream(client.getOutputStream())

        val file: File
        var fileBytes = ByteArray(0)
        try {
            file = File(request.second[0])
            fileBytes = Files.readAllBytes(file.toPath())
        } catch (e: Exception) {
            //dataOutputStream.write(Utils.packError())
            println("RRQ from ${client.localAddress}: File not found.") //ну так себе выводит
        }
        var currentBlock: Short = 1
        while (true) {
            if (Utils.DATA_SIZE * currentBlock > fileBytes.size) {
                dataOutputStream.write(
                    Utils.packDataBlock(
                        currentBlock, fileBytes.copyOfRange(
                            Utils.DATA_SIZE * (currentBlock - 1), fileBytes.size)
                    )
                )
                break
            } else {
                dataOutputStream.write(
                    Utils.packDataBlock(
                        currentBlock, fileBytes.copyOfRange(
                            Utils.DATA_SIZE * (currentBlock - 1), Utils.DATA_SIZE * currentBlock
                        )
                    )
                )
            }
            val input = ByteArray(Utils.PACKET_SIZE)
            try {
                dataInputStream.read(input)
            } catch (e: SocketException) {
                TODO("Not yet implemented")
            }
            when (input[1].toShort()) {
                Utils.Opcode.ACK.code -> {
                    currentBlock = Utils.unpackACK(input)
                }
                else -> {
                    TODO("Not yet implemented")
                }
            }
        }
    }
}