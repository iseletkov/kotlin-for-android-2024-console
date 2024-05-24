import kotlinx.coroutines.*
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach
import java.net.URI
import java.net.URL
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.*

abstract class CPerson(
    var name : String,
    var url: URL,
    var id : UUID = UUID.randomUUID()
)
{
    abstract fun print()
}

class CManager(
    name: String,
    url: URL,
    var organizationalUnit: String
) : CPerson(
    name,
    url
) {
    override fun print() {
        println("| $id | $name | $url | $organizationalUnit |")
    }
}


class CMain
{
    private fun outMenu()
    {
        println()
        println()
        println("Выберите требуемое действие: ")
        println("1. Вывести данные на экран.")
        println("2. Добавить сотрудника.")
        println("3. Добавить руководителя.")
        println("4. Удалить запись.")
        println("5. Фильтровать.")
        println("6. Отсортировать.")
        println("7. Скачать аватарки.")
        println("8. Выход.")
    }
    private fun getAction() : Int
    {
        var action : Int
        while (true) {
            outMenu()
            try{
                action = readln().toInt()
                if (action in 1..8)
                    return action
                println("Необходимо указать номер операции от 1 до 8!")
            }
            catch(e : Exception)
            {
                println("Необходимо указать номер операции от 1 до 8!")
            }
        }

    }
    private val persons = mutableListOf<CPerson>(
        CManager(
            "Иванов Иван Иванович",
            URI("https://random.imagecdn.app/1280/960").toURL(),
            "Отдел поддержки ИТ систем"
        ),
        CManager(
            "Петров Пётр Петрович",
            URI("https://random.imagecdn.app/360/240").toURL(),
            "Отдел продаж"
        )
    )

    private fun outData()
    {
        persons.forEach { it.print() }
    }
    private fun addManager()
    {
        println("Ф.И.О.: ")
        val name = readln()
        var url : String
        var urlCleared : URL
        while (true) {
            println("Ссылка на аватарку: ")
            url = readln()
            try{
                urlCleared = URI(url).toURL()
                break
            }
            catch (e : Exception)
            {
                println("Проверьте правильность указания http адреса!")
            }
        }
        println("Подразделение: ")
        val ou = readln()
        persons.add(
            CManager(
                name,
                urlCleared,
                ou
            )
        )
    }

    private fun filterData()
    {
        print("Введите Ф.И.О. для поиска: ")
        val filterStr = readln()

        val filtered = persons
            .filter {
                it.name.startsWith(filterStr, ignoreCase = true)
            }
        filtered
            .forEach { it.print() }
        if (filtered.isEmpty())
            println("По вашему запросу ничего не найдено!")

    }
    suspend fun downloadFile(url: URL, outputFileName: String) {
        //withContext(Dispatchers.IO) {
            url.openStream().use {
                Files.copy(it, Paths.get(outputFileName), StandardCopyOption.REPLACE_EXISTING)
            }
        //}
        //println(outputFileName)
    }
    private suspend fun downloadFileWC(url: URL, outputFileName: String) {
        withContext(Dispatchers.IO) {
            url.openStream().use {
                Files.copy(it, Paths.get(outputFileName), StandardCopyOption.REPLACE_EXISTING)
            }
        }
    }
    private fun loadAvatars()
    {
        println("Началась загрузка аватарок.")
        runBlocking {
//            persons
//                .asFlow()
//                .onEach { person ->
//                    downloadFile(person.url, "${person.id} ${person.name}.jpg")
//                }
//                .flowOn(Dispatchers.IO)
//                .collect {
//                    println("Загрузка завершена: ${it.name}")
//                }
            persons
                .map { person ->
                    async {
//                        if (person.name.startsWith("И"))
//                            delay(6000)

                        downloadFileWC(person.url, "${person.id} ${person.name}.jpg")
                        person
                    }
                }
//                .onEach {
//                    println("Загрузка объекта завершена: ${it.await().name}")
//                }
                .awaitAll()
        }
        println("Загрузка завершена.")
    }
    fun main()
    {
        while(true) {
            when(getAction())
            {
                1 -> outData()
                3 -> addManager()
                5 -> filterData()
                7 -> loadAvatars()
                8 -> return

            }

        }
    }
}

fun main()
{
    CMain().main()
}