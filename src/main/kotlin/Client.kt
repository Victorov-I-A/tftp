import java.io.*
import java.net.Socket
import java.nio.file.Files
import java.util.*

fun main(args: Array<String>) {
    val c = Client();
    c.start()
}

//C:/Users/Ilia/k.png
class Client {
    fun start() {
        println("format: [r | w] pathname")
        while (true) {
            val input = readLine()?.trim()?.replace("\\s+".toRegex(), " ")

            val splittedInput = input?.split(" ")
            if (splittedInput?.get(0) == "") {
                continue
            }

            if (splittedInput?.size == 1) {
                when ((splittedInput[0])) {
                    "exit" -> {
                        return
                    }
                    else -> {
                        println("format: [r | w] pathname")
                        continue
                    }
                }
            }
            if (splittedInput?.size == 2) {
                val inp0 = splittedInput[0]
                val inp1 = splittedInput[1]
                when (inp0) {
                    "r" -> {
                        readFile(inp1)
                    }
                    "w" -> {
                        writeFile(inp1)
                    }
                    else -> {
                        println("format: [r | w] pathname")
                        continue
                    }
                }

            }
            if (splittedInput?.size!! > 2) {
                println("format: [r | w] pathname")
                continue
            }

        }
    }

    private fun readFile(filePath: String) {
        val client = Socket("127.0.0.1", 9999)
        val dataOutputStream = DataOutputStream(client.getOutputStream())
        val dataInputStream = DataInputStream(client.getInputStream())

        val request = Utils.packRequest(Utils.Opcode.RRQ, filePath)
        dataOutputStream.writeInt(request.size)
        dataOutputStream.write(request)

        val byteStream = ByteArrayOutputStream()
        var currentBlock: Short = 1
        while (true) {
            val arraySize = dataInputStream.readInt();
            val input = ByteArray(arraySize)
            dataInputStream.read(input)

            val inputOnlyData = ByteArray(arraySize - 4)
            System.arraycopy(input, 4, inputOnlyData, 0, arraySize - 4);
            byteStream.write(inputOnlyData)

            print(input[2])
            print(" ")
            print(input[3])
            println()

            if (input[1].toShort() == Utils.Opcode.Data.code) {
                val packData = Utils.unpackData(input)
                if (currentBlock == packData.first) {             //всегда же тру?
                    currentBlock++
                    if (packData.second.size < 512)
                        break
                }
                println("pacack")
                val toWrite = Utils.packACK(currentBlock)
                dataOutputStream.writeInt(toWrite.size)
                dataOutputStream.write(toWrite)
            }
        }
        client.close()
        val fileName = filePath.split('/').last()
        File(fileName).writeBytes(byteStream.toByteArray())
        println("File $fileName downloaded successfully.")
    }

    private fun writeFile(filePath: String) {
        val client = Socket("127.0.0.1", 9999)
        val dataOutputStream = DataOutputStream(client.getOutputStream())
        val dataInputStream = DataInputStream(client.getInputStream())

        val request = Utils.packRequest(Utils.Opcode.WRQ, filePath)
        dataOutputStream.writeInt(request.size)
        dataOutputStream.write(request)
        println(request.toString(Charsets.UTF_8))

        val file = File(filePath)
        val fileBytes = Files.readAllBytes(file.toPath()) //кучи говн
        println(request)
        var remainingBytes = fileBytes.size//оставшиеся байты
        var currentBlock: Short = 1//текущий блок

        while (true) {
            val arraySize = dataInputStream.readInt();
            val readValue = ByteArray(arraySize)
            dataInputStream.read(readValue)

            println(currentBlock)
            println(remainingBytes)

            var minn = 512 * currentBlock
            if (remainingBytes < 512) minn = remainingBytes + 512 * (currentBlock - 1)
            println(minn)
            println("_____")
            val blockData = Arrays.copyOfRange(
                fileBytes,
                512 * (currentBlock - 1), minn
            )

            val z = Utils.packDataBlock(currentBlock, blockData)
            dataOutputStream.writeInt(z.size)
            dataOutputStream.write(z)


            remainingBytes -= 512
            currentBlock++
            if (remainingBytes <= 0) break
        }
    }

    //ыаы ты думал тут что-то будет?)))000))да.
    private fun closeClient() {
        println(123)
        TODO("Not yet implemented")
    }

    private fun closeServer() {
        TODO("Not yet implemented")
    }
}