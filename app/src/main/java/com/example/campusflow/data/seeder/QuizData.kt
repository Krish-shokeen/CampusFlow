package com.example.campusflow.data.seeder

import com.example.campusflow.data.entities.QuizQuestion

object QuizData {
    val questions = listOf(
        // Kotlin Category
        QuizQuestion(0, "Kotlin", "Which keyword is used for a read-only variable?", "var", "val", "let", "const", "B", "val creates an immutable reference."),
        QuizQuestion(0, "Kotlin", "How do you handle null safety in Kotlin?", "try-catch", "Optional", "?.", "null-check", "C", "The safe call operator ?. is used for null safety."),
        QuizQuestion(0, "Kotlin", "What is a suspend function?", "A background thread", "A function that pauses execution", "A loop", "An error handler", "B", "Suspend functions can pause and resume execution in coroutines."),
        QuizQuestion(0, "Kotlin", "Which scope is typically used for UI-related coroutines?", "GlobalScope", "viewModelScope", "runBlocking", "MainScope", "B", "viewModelScope binds coroutines to the ViewModel lifecycle."),
        QuizQuestion(0, "Kotlin", "What does the 'data' keyword do in a class?", "Creates a database table", "Generates equals, hashCode, and toString", "Encrypts data", "Parses JSON", "B", "Data classes automatically derive standard utility methods."),
        QuizQuestion(0, "Kotlin", "How do you declare a singleton in Kotlin?", "class Singleton", "singleton class", "object", "static class", "C", "The 'object' declaration defines a singleton class inherently."),
        QuizQuestion(0, "Kotlin", "Which collection type ensures unique elements?", "List", "Array", "Set", "Map", "C", "A Set contains unique elements only."),
        QuizQuestion(0, "Kotlin", "What is the primary use of StateFlow?", "State management in coroutines", "File I/O", "Database indexing", "Network routing", "A", "StateFlow is a state-holder observable flow."),
        QuizQuestion(0, "Kotlin", "What is the equivalent of 'switch' in Kotlin?", "match", "when", "choose", "select", "B", "The 'when' expression replaces the C-style switch statement."),
        QuizQuestion(0, "Kotlin", "Which extension function allows executing code on an object and returns the object itself?", "let", "run", "apply", "with", "C", "'apply' runs the block inside the object context and returns the object."),
        
        // Java Category
        QuizQuestion(0, "Java", "Which keyword is used to inherit a class?", "implements", "inherits", "extends", "super", "C", "The 'extends' keyword inherits from a superclass."),
        QuizQuestion(0, "Java", "What is the default value of a boolean variable?", "true", "false", "null", "0", "B", "Boolean defaults to false in Java."),
        QuizQuestion(0, "Java", "Which of these is NOT a Java primitive type?", "int", "float", "String", "char", "C", "String is an Object, not a primitive type."),
        QuizQuestion(0, "Java", "What is the size of an int in Java?", "8 bits", "16 bits", "32 bits", "64 bits", "C", "An int in Java is a 32-bit signed integer."),
        QuizQuestion(0, "Java", "Which method is the entry point of a Java program?", "start()", "init()", "main()", "run()", "C", "public static void main(String[] args) is the entry point."),
        QuizQuestion(0, "Java", "What is polymorphism?", "Multiple inheritances", "Many forms", "Data hiding", "Code duplication", "B", "Polymorphism means 'many forms', allowing method overriding and overloading."),
        QuizQuestion(0, "Java", "Which exception is thrown when dividing by zero?", "NullPointerException", "ArithmeticException", "IOException", "NumberFormatException", "B", "Dividing an integer by zero throws ArithmeticException."),
        QuizQuestion(0, "Java", "Which collection allows key-value pairs?", "ArrayList", "LinkedList", "HashMap", "HashSet", "C", "HashMap stores items in key/value pairs."),
        QuizQuestion(0, "Java", "What does 'static' mean?", "Belongs to the instance", "Belongs to the class", "Cannot change", "Is abstract", "B", "Static members belong to the class, not individual objects."),
        QuizQuestion(0, "Java", "Which access modifier is the most restrictive?", "public", "protected", "default", "private", "D", "Private restricts access only to the defining class."),

        // DBMS Category
        QuizQuestion(0, "DBMS", "What does SQL stand for?", "Standard Query Language", "Structured Query Language", "Simple Query Logic", "System Query Link", "B", "Structured Query Language."),
        QuizQuestion(0, "DBMS", "Which key uniquely identifies a row?", "Foreign Key", "Primary Key", "Super Key", "Candidate Key", "B", "Primary Key strictly identifies unique records."),
        QuizQuestion(0, "DBMS", "What is a Foreign Key?", "A key from an external DB", "A key matching another table's Primary Key", "An encrypted key", "A composite key", "B", "It creates relationships between tables."),
        QuizQuestion(0, "DBMS", "Which normal form removes partial dependency?", "1NF", "2NF", "3NF", "BCNF", "B", "Second Normal Form removes partial dependencies."),
        QuizQuestion(0, "DBMS", "What command is used to remove a table?", "DELETE", "DROP", "REMOVE", "TRUNCATE", "B", "DROP deletes the table structure and data."),
        QuizQuestion(0, "DBMS", "ACID stands for Atomicity, Consistency, Isolation, and...?", "Durability", "Data", "Dependency", "Dynamics", "A", "Durability guarantees committed transactions survive failures."),
        QuizQuestion(0, "DBMS", "Which JOIN returns all rows when there is a match in either table?", "INNER JOIN", "LEFT JOIN", "RIGHT JOIN", "FULL OUTER JOIN", "D", "FULL OUTER JOIN returns all matching and non-matching rows."),
        QuizQuestion(0, "DBMS", "What is a View?", "A physical table", "A virtual table based on a query", "A database index", "A trigger", "B", "A View dynamically represents data from one or more tables."),
        QuizQuestion(0, "DBMS", "What removes all records but keeps the structure?", "DROP", "DELETE", "TRUNCATE", "CLEAR", "C", "TRUNCATE is a fast way to empty a table."),
        QuizQuestion(0, "DBMS", "Which clause is used to filter records after GROUP BY?", "WHERE", "ORDER BY", "HAVING", "FILTER", "C", "HAVING filters aggregated grouped data."),

        // Data Structures Category
        QuizQuestion(0, "Data Structures", "Which DS uses LIFO?", "Queue", "Stack", "Tree", "Graph", "B", "Stack follows Last-In-First-Out."),
        QuizQuestion(0, "Data Structures", "Which DS uses FIFO?", "Queue", "Stack", "Tree", "Array", "A", "Queue follows First-In-First-Out."),
        QuizQuestion(0, "Data Structures", "What is the time complexity of binary search?", "O(1)", "O(n)", "O(log n)", "O(n^2)", "C", "Binary search halves the search space each step."),
        QuizQuestion(0, "Data Structures", "Which sorting algorithm is fastest on average?", "Bubble Sort", "Insertion Sort", "Selection Sort", "Quick Sort", "D", "Quick Sort has an average case of O(n log n)."),
        QuizQuestion(0, "Data Structures", "A Tree with maximum 2 children per node is?", "Binary Tree", "B-Tree", "AVL Tree", "Trie", "A", "A Binary Tree allows at most two children."),
        QuizQuestion(0, "Data Structures", "Which traversal visits Left-Root-Right?", "Pre-order", "In-order", "Post-order", "Level-order", "B", "In-order traversal processes the left child, root, then right child."),
        QuizQuestion(0, "Data Structures", "What is an AVL tree?", "A balanced binary search tree", "A complete tree", "A graph", "A heap", "A", "AVL trees self-balance their heights."),
        QuizQuestion(0, "Data Structures", "Which DS represents a network of nodes?", "Array", "Stack", "Graph", "Queue", "C", "Graphs connect vertices via edges."),
        QuizQuestion(0, "Data Structures", "What data structure does DFS use?", "Queue", "Stack", "Heap", "Hash", "B", "Depth-First Search naturally uses a Stack (or recursion)."),
        QuizQuestion(0, "Data Structures", "What data structure does BFS use?", "Queue", "Stack", "Heap", "Hash", "A", "Breadth-First Search uses a Queue to explore level by level."),

        // General Computer Science
        QuizQuestion(0, "General Computer Science", "What does CPU stand for?", "Central Process Unit", "Central Processing Unit", "Computer Personal Unit", "Central Processor Unit", "B", "Central Processing Unit is the brain of the computer."),
        QuizQuestion(0, "General Computer Science", "How many bytes are in a kilobyte (KB)?", "1000", "1024", "1048", "2048", "B", "1 KB equals 1024 bytes in binary terms."),
        QuizQuestion(0, "General Computer Science", "What is the main function of an OS?", "Compile code", "Manage hardware and software resources", "Edit texts", "Browse internet", "B", "An Operating System manages computer resources."),
        QuizQuestion(0, "General Computer Science", "Which of these is a low-level language?", "Python", "Java", "Assembly", "C++", "C", "Assembly communicates very closely with hardware."),
        QuizQuestion(0, "General Computer Science", "What does HTTP stand for?", "HyperText Transfer Protocol", "HyperText Transmission Protocol", "HyperText Transfer Package", "HyperLink Transfer Protocol", "A", "HTTP is the foundation of data communication for the web."),
        QuizQuestion(0, "General Computer Science", "What is an IP address?", "A physical hardware identifier", "A unique network identifier", "A web browser", "A programming language", "B", "IP addresses route data across networks."),
        QuizQuestion(0, "General Computer Science", "What does GUI stand for?", "Graphical User Interface", "General User Interface", "Graphic Utility Integration", "General Utility Interface", "A", "GUI allows visual interaction with software."),
        QuizQuestion(0, "General Computer Science", "What translates source code into machine code completely before execution?", "Interpreter", "Compiler", "Assembler", "Debugger", "B", "Compilers convert the entire source code upfront."),
        QuizQuestion(0, "General Computer Science", "What is 'Cloud Computing'?", "Weather prediction software", "Local network storage", "On-demand internet resources", "A brand of hard drives", "C", "Cloud computing provides resources like storage and servers over the internet."),
        QuizQuestion(0, "General Computer Science", "Which component stores short-term working data?", "Hard Drive", "ROM", "RAM", "GPU", "C", "RAM (Random Access Memory) is volatile short-term memory.")
    )
}