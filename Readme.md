
Definiciones
------------
En el ecosistema de un BPM, en este caso de Drools (JBPM) encontramos varios conceptos y agentes que intervienen en su configuración y ejecución.

* **Facts**: son los datos de entrada. Pueden ser POJOs, Clases Servicio, utilidades, basicamente lo que queramos.Tambien podremos definirlas dentro de drools directamente.

* **Rule**: Reglas, indican cuando se deben aplicar y que se debe ejecutar. Tiene 2 partes fundamentales
	* RHS: Right Hand Side, donde se definen los criterios que la dispararán. 
	* LHS: Left Hand Side, donde se definen las acciones que se ejecutarán.
	 
	 La definición de las reglas puede implementarse de 2 maneras, mediante:

	- *Excel (.xls, .xlsx)*. Tambien llamadas Decision Tables . Nuestras reglas se pueden definir en este tipo de ficheros mas amigables para perfiles no tan tecnicos, que solo quieran precuparse de definir las reglas de negocio abstrayendose de todo lo que hay por debajo.

	- *Ficheros .drl (Drools Rule File)*: ficheros de texto plano mucho mas versatiles donde se puede especificar a bajo nivel comportamientos que no seriamos capaces de definir en un amigable xls/xlsx.

* **WorkingMemory**: memoria de trabajo, es el contexto de ejecucion. En el se evaluan y ejecutan las reglas.

* **KnowledgeSession**: La session se crea dentro de la Working Memory. Esta es la encargada de la preparacion de la ejecucion y montar el ecosistemas de reglas asociadas a la ejecucion.

* **KnowledgeBase**: Repositorio donde encontrar las reglas y construir la engine , es decir la Knowledge Session.Como puedes imaginar, ese repositorio puede nutrirse de diferentes fuentes de donde leer la definicion de las reglas: de un directorio local, un repositorio remoto centralizado y versionado de las reglas (, o al fin de alcabo cualquier cosa convertible a un array de bytes ... 

*El proyecto Business Central Workbench WildFly WAR servido a traves del KIE Execution Server (Wildfly Application server) de la gente de JBoos nos permite desplegar un servidor centralizado de reglas, donde podemos editar, versionar y servir configuracion de una manera centralizada a distintos servidores de ejecucion.*

La forma en como se buscan, indexan , interpretan las definiciones de reglas, el formato de contexto a usar (stateless/statefull), en definitiva la forma en la que el BPM compila las reglas y los POJOs es totalmente editable desde el API que nos proporciona jBPM y Drools. Como puedes imaginar, al tener acceso al compilador, puedes definir tipos dentro de drools que en tiempo de compilacion seran añadidos, edición de prototipos de clase y un largo etc...

### KIE (Knowledge Is Everything)
Si nuestro foco esta basado solo en el uso de la herramienta podemos usar "wrappers" de todo el ecosistema como es la inciativa KIE. 
Estas librerias nos abstraen y proporcionan las herramientas de ejecucion basicas para el uso del JBPM.


PREPARACION DEL ENTORNO
-----------------------
Podemos importar todas las dependencias desde los repositorios centrales de maven de manera agil :
```
<!-- JBPM and Spring integration -->
<dependency>
    <groupId>org.drools</groupId>
    <artifactId>drools-core</artifactId>
    <version>7.18.0.Final</version>
</dependency>
<dependency>
    <groupId>org.kie</groupId>
    <artifactId>kie-spring</artifactId>
    <version>7.18.0.Final</version>
</dependency>
```



PREPARANDO LA EJECUCION PARA UNOS TEST EXPLICATIVOS
---------------------------------------------------
Para que nos vayamos introduciendo en todo este mundo, vamos a hacer unos ejemplos muy sencillos donde para que aprendamos un poco a manejarnos
en todo este rollo de las reglas. Posteriormente intentaremos hacer un ejemplo mas "imaginativo" para que veamos como casaría aqui una aplicacion empresarial 
un poco mas "a lo grande".

Lo primero generar la configuracion. Para instanciar el motor con Spring Boot es bien sencillo:
generamos el archiconocido @Configuration que nos genere un KieContainer que encapsule el KnowledgeBase, 
luego lo usaremos para crear  KnowledgeSession en las ejecuciones:
```
@Configuration
public class BPMConfigurations {
	//lives on classpath , in this case into -- src/main/resources/rules/*.drl 
	private static final String[] drlFiles = { "rules/discountRules.drl" };
	@Bean
	public KieContainer kieContainer() {
		KieServices kieServices = KieServices.Factory.get(); 
		//Load Rules and Ecosystem Definitions
		KieFileSystem kieFileSystem = kieServices.newKieFileSystem();
		for (String ruleFile : drlFiles) {
			kieFileSystem.write(ResourceFactory.newClassPathResource(ruleFile));
		}
		//Generate Modules and all internal Structures
		KieBuilder kieBuilder = kieServices.newKieBuilder(kieFileSystem);
		kieBuilder.buildAll();
		KieModule kieModule = kieBuilder.getKieModule();
		return kieServices.newKieContainer(kieModule.getReleaseId());
	}
}
```
Como podemos ver KIE nos abstrae de configuracion de Modules BPM y demás configuracion subyacente. Con este Bean en el contexto ya podemos empezar a trastear.
Le especificamos que carge 1 ficheros de reglas que se encuentran en el classPath (src/main/resources/rules/discountRules.drl) y ya el KIEContainer hace todo por nosotros.


Ahora generatmos un POJO para actuar como Fact (ProductPrice.java) muy sencillo (he usado lombok framework para excluir codigo declarativo innecesario): 
```
@Getter @Setter @NoArgsConstructor @ToString
public class ProductPrice {
	private Integer basePrice;
	public ProductPrice(Integer basePrice) {
		this.basePrice=basePrice;
	}
}
```
Ahora una clase de Servicio muy sencilla que nos provea del acceso a la ejecucion 
```
@Service
public class PriceCalculatorService {
	@Autowired
    private KieContainer kieContainer;
	public void executeRules(ProductPrice productPrice) {
    	KieSession kieSession = kieContainer.newKieSession();
        kieSession.insert(productPrice);
        kieSession.fireAllRules();
        kieSession.dispose();
    }
```
Y por ultimo un punto de entrada para poder ejecutar nuestro codigo. Es este caso un JUnit test
```
@RunWith(SpringRunner.class)
@SpringBootTest
public class DroolsDemoApplicationTests {
	@Autowired
	private PriceCalculatorService priceCalculatorService;
	@Test
	public void executeCalculations() {
		ProductPrice productPrice = new ProductPrice(5);//Create the Fact
		priceCalculatorService.executeRules(productPrice);//Call service and internal BlackBox rules engine
	}
}
```
Vemos que instanciamos un precio de producto con basePrice = 5. Luego se lo pasamos al service, que ejecuta la caja negra del motor de reglas y fin.
con esto es mas que suficiente para hacer nuestras pruebas y juguetear un poco.

Vamos a ver el contenido de nuestra definicion de reglas del archivo discountRules.drl

discountRules.drl
------------------
empezamos con una version muy sencilla:
```
package myAppRules;

import com.dppware.droolsDemo.bean.*;

dialect  "mvel"

rule "Adjust Product Price"
    when
     	$p : ProductPrice(basePrice > 2 )
    then
    	System.out.println("EJECUTANDO -Adjust Product Price- para el producto [" + $p + "]");
end
```

Antes de ver en detalle cada parte, podemos intuir que importa unos tipos de objeto y que hay una rule que tiene una condicion y que si se cumple hace un system Out.
Por partes:
*package* : es una agrupacion logica de reglas.No tiene que ver con paqueteria física. Piensalo mas como un namespace donde los globals y scopes de grupos de  elementos tienen relacion (globas, functions y demas cosas quee veremos mas adelante). Los nombres de las rules deben ser unicos dentro de un mismo package (namespace)

*import* : importamos definiciones de clasess que necesitará drools en la compilacion de las reglas y su ejecucion. Por defecto indicar que drools importa siempre el paquete java.lang.* ,por lo que podremos usar todas las clases del paquete en nuestras definiciones de reglas .

*rule* : es el bloque de codigo que indica inicio y fin de una regla (rule): Como comenté al principio las reglas solo se componen de 2 partes fundamentales, 
	when (RHS - Right Hand Side): donde se definen los criterios que la dispararan. 
	then (LHS - Left Hand Side): donde se definen las acciones.

*dialect* : el tipo de lenguage usado para las definiciones dentro de las reglas. Los 2 mas extendidos son 
	"mvel"-> (MVFLEX Expression Language): Es un lenguage declarativo mas sencillo y su unica finalidad es hacer el codigo mas legible. Ofrece sintaxis que casa con la nomenclatura java standar de forma que abstrae de getter y setter. Su uso es casi extendido a la seccion RHS.  Hay ya muchos DSL que se basan en esto, pero pongo aqui un ejemplillo de "traduccion":
	  	java version: $person.getAddresses().get("home").setStreetName("my street");
		mvel version: $person.addresses["home"].streetName = "my street";
		Tambien nos permite asignacion de variables en el scope de una rule de manera sencilla ($varName), asi como la deficion de nuevos tipos(classes) de manera sencilla.
	"java"-> Pues Java. Es decir podemos incluir nuestra sintaxis java dentro del .drl.Su única restriccion es que solo se puede usar en el LHS (lefHandSide), es decir en el then.



Ejecutando
----------
Si ejecutamos el test, estos son los pasos fundamentales:
1 .Entra el fact a evaluar con baseprice = 5
		ProductPrice productPrice = new ProductPrice(5);//Create the Fact
		priceCalculatorService.executeRules(productPrice);//Call service and internal BlackBox rules engine
2. El motor de drools comprueba el when y como se cumple, pues asigna a la variable local $p el objeto ProductPrice. Mvel nos proporciona el acceso al getBasePrice sin necesidad de declararlo.
		$p : ProductPrice(basePrice > 2 )  // Object({conditions}) format (lo vamos a ver ahora mismito..)
3.Como se cumple ejecuta el then, que usa en este caso sintaxis Java y tiene acceso a la $p que sera de tipo ProductPrice


conditions (Revisando condiciones en LHS )
----------
Como hemos visto en el paso 2 anterior, se cumple la condicion de que basePrice > 2 (ya que era 5 cuando entro al contexto de ejecucion). 
Para el ejemplo anterior podríamos condiciones mas complejas ( > , < , >=, =<, || , && , == , % , ^, contains, not contains, memberof, not memberof, matches (regExp), not matches (regExp), starswith , etc...) :
```
	$p : ProductPrice(((basePrice / 5) == 1) && ((basePrice % 5) == 0 ))
	etc...
``` 		
en vez de anidarlos tambien podemos usar la clausula por defecto and y convertir la condicion anterior en 
```
	when
		$p : ProductPrice((basePrice / 5) == 1))
		$p : ProductPrice((basePrice % 5) == 0 ) //se deben cumplir todas
	then
``` 
Existe una evaluacion en modo manual que evalua a una expresion booleana la ejecucion de una regla:
```
	when
		eval(true)
		eval(callMyCustomFunctionThatReturnsABoolean)
	then
``` 
en el segundo eval, dejo ver que Drools como compilador tambien nos permite referenciar a metodos staticos de nuestro codigo o definir funciones dentro del mismo fichero .drl que como ya comente estaran disponibles en todo el package (namespace) (dentro de unas lineas lo vamos a ver..)


modify (Aplicando ordenes en el then RHS )
-------------------------------------
Muy bien ya hemos visto las conditios pero vamos a ver la órdenes. Hemos visto sencilla ejecucion con el system out, referenciando a la variable local de la rule:
```
    then
    	System.out.println("EJECUTANDO -Adjust Product Price- para el producto [" + $p + "]");
```
si queremos aplicar una modificacion vamos a usar el bloque modify. Vamos a bajarle el basePrice un punto, nos quedara asi:
```
import com.dppware.droolsDemo.bean.*;
dialect  "mvel"
rule "Adjust Product Price"
	when
     	$p : ProductPrice(basePrice > 2 )
    then
    	modify($p){
	    	setBasePrice($p.basePrice - 5);
	    }
	    System.out.println("el precio ajustado es " + $p.basePrice);
end
```
Sencillo verdad? abrimos un bloque modify con el scope de la variable definida (ya que se cumplio el LHS) y en el contexto encontramos la funcion setBasePrice del objeto ProductPrice.
Si metes esta regla y ejecutas veras este output:

el precio ajustado es 0



Rules Attributes
-----------------
Como comente antes, el comportamiento en runtime de una rule se puede restringir. Para ello debemos usar los rule atributes que nos ofrece drools:
```
rule 'rulename'
	//rules Atributtes (availables: no-loop, salience, ruleflow-group, lock-on-active, agenda-group, 
	// activation-group, auto-focus, dialect, date-effective, date-expires, duratio, timer, calendars
	when
	then:end
```	
Vamos a comentar 2 de ellas (las que me parecen mas genericas y obligatorias de conocer), si quieres profundizar tienes la referencia oficial aqui https://docs.jboss.org/drools/release/5.2.0.Final/drools-expert-docs/html/ch05.html#d0e3761

#No-loop
Imaginemos que metemos un Fact (ProductPrice.java) que su basePrice = 5 , cuando se cumple la condicion solo le restamos 1. Drools por defecto volvera a evaluar la LHS y verá que sigue cumpliendose
asi que la regla será disparada varias veces, hasta que no se cumpla la condicion.
```
rule "Adjust Product Price"
	when
			$p : ProductPrice(basePrice > 2 )
	then		
		modify($p){
	    	setBasePrice($p.basePrice -1);
	    }
	    System.out.println("el precio ajustado es " + $p.basePrice);
end
```
Ouput:
el precio ajustado es 4
el precio ajustado es 3
el precio ajustado es 2

Esto puede ser un problema potencial, porque si por ejemplo la ejecucion es de tipo void y no se modifica el Fact, entrariamos en un bucle infinito.
Para asegurar que la regla (si se cumple) solo se ejecute una vez, usamos el atributo no-loop en la definicion de la regla y  vemos que podemos cambiar el comportamiento de la ejecucion:
rule "Adjust Product Price"
	no-loop
    when
     	$p : ProductPrice(basePrice > 2 )
    then
    	modify($p){
	    	setBasePrice($p.basePrice -1);
	    }
	    System.out.println("el precio ajustado es " + $p.basePrice);
end
Output:
el precio ajustado es 4

Solo se ha ejecutado 1 vez ya que solo se ha evaluado 1 vez.

#Salience
Por defecto Drools ordena las reglas de ejecucion en el orden que se las va encontrandoal parsear el fichero de reglas, de modo que en tiempo de ejecucion
se ejecutaran las reglas que su RHS se cumpla y por el orden en el que se encontraron: 
Imagina esta definicion de drl en la que hemos introducido 2 reglas: 
```	
rule "Adjust Product Price"
	no-loop
    when
     	$p : ProductPrice(basePrice > 2 )
    then
    	System.out.println("EJECUTANDO -Adjust Product Price-");
end
rule "Sending Notification"
	no-loop
    when
     	$p : ProductPrice(basePrice > 2 )
    then
    	System.out.println("EJECUTANDO -Sending Notification-");
end
```	

Output-
EJECUTANDO -Adjust Product Price-
EJECUTANDO -Sending Notification-

Salience es un sistema de pesos que nos permite indicar prioridades sobre ejecuciones de las reglas en caso de coincidencia.
Si añadimos el atributo salience a las reglas vemos como podemos especificar el orden
```	
rule "Adjust Product Price"
	no-loop
	salience 1
    when
     	$p : ProductPrice(basePrice > 2 )
    then
    	System.out.println("EJECUTANDO -Adjust Product Price-");
end
rule "Sending Notification"
	no-loop
	salience 2
    when
     	$p : ProductPrice(basePrice > 2 )
    then
    	System.out.println("EJECUTANDO -Sending Notification-");
end
```	
Output-
EJECUTANDO -Sending Notification-
EJECUTANDO -Adjust Product Price-

Ahora se han ejecutado ordenadamente en funcion del peso(valor) de nuestra rule dentro del engine.


USANDO EL COMPILADOR
--------------------
Como comenté anteriormente, podemos importar funciones, crear nuevos tipos,  funciones dentro del drl, etc.. 

# functions - > Importando metodos de utilidades:
Pongamos que queremos tener un modo de loggin customizado en nuestra aplicacion (o lo que sea que haga el metodo, quizas porque no nos apañamos metiendo la logica en el drl) y que queremos usarlo dentro de nuestra regla.
Pues es tan facil como definir el metodo con acceso static en nuestra clase java y luego importarlo en nuestro fichero .drl (recuerda que estara disponible para todo el package):
```	
public class Utils {
	public static void prettyTraces(Object message) {
		System.out.println("PrettyTraces -> ***"+message+"***");
	}
}
```	
Para usarla en nuestra rule basta con importarlo de manera declarativa completa. Basandonos en las rules que estabamos usando nos quedaria asi:
```	
import com.dppware.droolsDemo.bean.*;

//Imported specified functions
import function com.dppware.droolsDemo.utils.Utils.prettyTraces;

dialect  "mvel"

rule "Adjust Product Price"
	when
     	$p : ProductPrice(basePrice > 2 )
    then
    	modify($p){
	    	setBasePrice($p.basePrice - 5);
	    }
	    prettyTraces("el precio ajustado es " + $p.basePrice);
end
```	

Output:
PrettyTraces -> ***el precio ajustado es 0***

Tambien puedes observar que he metido // para los comentarios
# functions - > Creandolas en drl:
Definir funciones dentro del .drl es muy sencillo. Pero en su declaracion de tipo usamos la palabra reservada funcion, vamos a incrementar el codigo metiendo la definicion tambien:
```
import com.dppware.droolsDemo.bean.*;

//Imported specified functions
import function com.dppware.droolsDemo.utils.Utils.prettyTraces;

dialect  "mvel"

//functions inline definition
function Integer calculateIncrement(Integer value, int quantity) {
    return value + quantity;
}



rule "Adjust Product Price"
	when
     	$p : ProductPrice(basePrice > 2 )
    then
    	modify($p){
	    	setBasePrice($p.basePrice - 5);
	    }
	    prettyTraces("el precio ajustado es " + $p.basePrice);
	    prettyTraces(calculateIncrement($p.basePrice, 3));
end
```
Output:
PrettyTraces -> ***el precio ajustado es 0***
PrettyTraces -> ***3***


vemos como hacemos la llamada al metodo recien definido y ademas al prettyTraces que añadimos anteriormente.


#Nuevos Tipos (TYPE - class ) 
Es posible definir nuestras nuevas classes dentro del ecosistema de manera declarativa y sin compilacion previa, ya que el compilador leera la definicion
e introducira en el classloader las definiciones para la instanciacion. 
La declaracion de nuevos tipos se hace en la misma seccion de declaracion de las funciones, usando la palabra reservada "declare".
Por defecto con el uso de "mvel" los getters/setters/toString/equals/hashCode seran añadidos a la definicion.
El compilador creará 2 constructores por defecto (uno vacio y otro con todos los campos como argumentos).
Si queremos customizar el constructor para usar solo determinados argumentos debemos usar la anotación @key, puedes mas ejemplos en la documentacion oficial aqui
https://docs.jboss.org/drools/release/5.2.0.Final/drools-expert-docs/html/ch05.html#d0e3418 ).
```
declare Product
   code : int
   name : String
   description : String
ends```
Y para su instanciacion se puede hacer de esta manera usando sintaxis java (recuerda que esta sintaxis solo es aceptada en RHS), y si lo piensas bien, 
solo tiene sentido instanciar objetos ahi, ya que el then debe ser usado unicamente para el analisis de los Facts.

Ahora el .drl tendra este aspecto:
```
import com.dppware.droolsDemo.bean.*;

//Imported specified functions
import function com.dppware.droolsDemo.utils.Utils.prettyTraces;

dialect  "mvel"

//functions inline definition
function Integer calculateIncrement(Integer value, int quantity) {
    return value + quantity;
}

//New Types definition
declare Product
   code : int
   name : String
   description : String
end

rule "Adjust Product Price"
	when
     	$p : ProductPrice(basePrice > 2 )
    then
    	modify($p){
	    	setBasePrice($p.basePrice - 5);
	    }
	    prettyTraces("el precio ajustado es " + $p.basePrice);
	    prettyTraces(calculateIncrement($p.basePrice, 3));
	    //Instanciacion y print del object
	    Product pro = new Product();
	    pro.setCode(3321);
	    pro.setName("Leche");
	    pro.setDescription("Rica en Calcio");
	    prettyTraces(pro);
	    
end
```

Y el Output al ejecutarlo es 

PrettyTraces -> ***el precio ajustado es 0***
PrettyTraces -> ***3***
PrettyTraces -> ***Product( code=3321, name=Leche, description=Rica en Calcio )***

Como podemos observar estamos "uglificando" el codigo en este archivo .drl. Entonces empieza a tener sentido el concepto de "package" , porque podemos
tener varios ficheros .drl (uno con definicione de tipos, otro con funciones y otro con las rules...) y al compartir package (namespace) estaran disponibles.
En la siguiente seccion veremos como incluir varios ficheros , pero antes vamos a ver los "Global" que va muy relacionado con lo que acabamos de ver.

#Importar objects desde el contexto java (globals)
Hemos visto ya que podemos :
-Definir nuestras funciones (function)
-Definir nuestros Typos (classes)
-importar tipos para usarlos en nuestros .drl (import pck.subpck1.subpck2.className )

Y ahora vamos a ver como meter objetos ya instanciados y disponibles en la maquina virtual al contexto de ejecucion de Drools.
Puede ser muy util injectar un bean de servicio de nuestro contexto de Spring para que sea utilizado en las LHS para realizar operaciones
complejas.

A este tipo de inyecciones se las denomina **globals**. Para estas si que necesitamos editar nuestro test Java ya que se lo pasamos como 
argumento en la construccion de la session de ejecucion.

Definimos un @Service Java y le metemos un metodo muy basico que simule que publica en un topic, es solo para que te hagas una idea:
```
@Service
public class PushSubService {
	//@Autowired
	//private KafkaTemplate<String, String> kafkaTemplate;
	public void publishNewProductCreated(Object o) throws JsonProcessingException {
		String rawJSON = new ObjectMapper().writeValueAsString(o);
		//kafkaTemplate.send("newProduct", rawJSON); ...or whatelse
		System.out.println("Publishing newProduct Topic , content ["+rawJSON+"]");
	}
}
```
Ahora nos interesa tener este objeto dentro del contexto de ejecucion de las rules de drools, asi que lo inyectamos como global cuando solicitamos la ejecucion, 
La clase PriceCalculatorService.java (si recuerdas :-D ) llevamos un buen rato sin tocarla y en mi opinion esto es lo bueno de drools, que solo montamos los "hierros" en java y lo delegamos todo en el motor de reglas. La modificamos añadiendo "globals" a traves de la KieSession, nos quedara asi:
```
	@Autowired
	private PushSubService pushSubService;
	public void executeRules(ProductPrice productPrice) {
    	KieSession kieSession = kieContainer.newKieSession();
    	kieSession.setGlobal("publishTool", pushSubService);//adding globals
    	kieSession.insert(productPrice);
        kieSession.fireAllRules();
        kieSession.dispose();
    }
```
He inyectado el servicio a traves del global name "publishTool" y lo referencio dentro del .drl en la parte de definiciones que estabamos usando para
las functions y los tipos que vimos anteriormente.
```
global com.dppware.droolsDemo.services.PushSubService publishTool;
```
y lo uso en alguna RHS (then) para comprobar que podemos usarlo:
```
publishTool.publishNewProductCreated(pro); 
```

El archivo .drl ahora tiene esta forma, vemos que metemos la ultima ejecucion en la que usamos el global para publicar.:```
import com.dppware.droolsDemo.bean.*;

//Imported specified functions
import function com.dppware.droolsDemo.utils.Utils.prettyTraces;

//global Sets
global com.dppware.droolsDemo.services.PushSubService publishTool;

dialect  "mvel"

//functions inline definition
function Integer calculateIncrement(Integer value, int quantity) {
    return value + quantity;
}

//New Types definition
declare Product
   code : int
   name : String
   description : String
end

rule "Adjust Product Price"
	when
     	$p : ProductPrice(basePrice > 2 )
    then
    	modify($p){
	    	setBasePrice($p.basePrice - 5);
	    }
	    prettyTraces("el precio ajustado es " + $p.basePrice);
	    prettyTraces(calculateIncrement($p.basePrice, 3));
	    //Instanciacion y print del object
	    Product pro = new Product();
	    pro.setCode(3321);
	    pro.setName("Leche");
	    pro.setDescription("Rica en Calcio");
	    prettyTraces(pro);
	    publishTool.publishNewProductCreated(pro);
	    
end
```
Output:

PrettyTraces -> ***el precio ajustado es 0***
PrettyTraces -> ***3***
PrettyTraces -> ***Product( code=3321, name=Leche, description=Rica en Calcio )***
Publishing newProduct Topic , content [{"code":3321,"name":"Leche","description":"Rica en Calcio"}]



ORGANIZANDO EL CODIGO
---------------------
Hemos visto ya que tenemos un batiburrillo de declaraciones, imports, functions, declares, tipos, globals, rules...
y todo en el mismo archivo...bufff es una casa de locos. Vamos a estructurarlo un poco, ya no solo por tenerlo mas legible, si no
porque si sabemos dividir bien, podemos generar "ecosistemas" solamente juntando piezas (.drl) y lograr asi comportamientos portables
y flexibes.
La idea de dividir en archivos, separando las reglas en un .drl, las definiciones de tipos en otro, las funciones en otro, etc.. 
nos va a generar una dependencia entre imports de ficheros , todavia no hay un sistema de dependencias entre 
ficheros de reglas y lo deberiamos de gestionar nosotros a mano :-( . 
Por otro lado estructurar un poco el .drl tambien creo que es una buena practica y  nos ayuda a mantener la atomicidad y coherencia del contexto
de las reglas. 
Yo voy a dividir en distintos archivos, para que se vea y entienda el concepto de package que comente al princpio y que no he "implementado/explicado" todavía por darle 
continuidad a este tutorial.
Tambien, habrá de los que piensen que meter los "global" crean una dependencia de ejecucion con elementos externos , totalmente de acuerdo. Tambien podríamos
optar por importar tipos del classpath y hacerlo todo en drools, pero con la facilidad que nos da Spring para un monton de cosas pues eso...para gustos los colores.

Como hemos visto, importar los archivos .drl en nuestro ecosistema, se reduce a importar un array de bytes, asi que podríamos optar por implementar un repositorio
central de ficheros .drl , versionable y que a parte nos provea de informacion de dependencias de archivos .drl... no sé, deja volar tu imaginación.. 

Generamos 4 archivos separando los elementos, pero ahora si que es obligatorio que **todos incluyan la definicion del mismo package** (que como comente al principio, solo es un namespace)

#product_beans.drl
Solo contiene informacion acerca de Tipos (objetos) necesarios para tareas internas.
```
package com.demo.product;
dialect  "mvel"
declare Product
   code : int
   name : String
   description : String
end
```

#product_dependencies.drl
Solo contiene informacion acerca de agentes externos (globals requeridos)
```
package com.demo.product;

//global Sets
global com.dppware.droolsDemo.services.PushSubService publishTool;

dialect  "mvel"
```

#product_functions.drl
Solo contiene informacion acerca de Tipos (objetos) necesarios para tareas internas.
```
package com.demo.product;

//Imported specified functions
import function com.dppware.droolsDemo.utils.Utils.prettyTraces;


//functions inline definition
function Integer calculateIncrement(Integer value, int quantity) {
    return value + quantity;
}

dialect  "mvel"
```
 
#product_rules.drl
Solo contiene informacion acerca las rules de negocio.
```
package com.demo.product;

import com.dppware.droolsDemo.bean.*;

dialect  "mvel"

/**
* javadoc description
**/
rule "Adjust Product Price"
	when
     	$p : ProductPrice(basePrice > 2 )
    then
    	modify($p){
	    	setBasePrice($p.basePrice - 5);
	    }
	    prettyTraces("el precio ajustado es " + $p.basePrice);
	    prettyTraces(calculateIncrement($p.basePrice, 3));
	    //Instanciacion y print del object
	    Product pro = new Product();
	    pro.setCode(3321);
	    pro.setName("Leche");
	    pro.setDescription("Rica en Calcio");
	    prettyTraces(pro);
	    publishTool.publishNewProductCreated(pro);
	    
end
```

Y para cargarlos en el engine, los añadimos en el @Configuration (BPMConfigurations.java)
```
@Configuration
public class BPMConfigurations {
	private static final String[] drlFiles = { 	"rules/product_beans.drl", 
												"rules/product_dependencies.drl",
												"rules/product_functions.drl",
												"rules/product_rules.drl"
											};
	@Bean
	public KieContainer kieContainer() {
		KieServices kieServices = KieServices.Factory.get(); 
		//Load Rules and Ecosystem Definitions
		KieFileSystem kieFileSystem = kieServices.newKieFileSystem();
		for (String ruleFile : drlFiles) {
			kieFileSystem.write(ResourceFactory.newClassPathResource(ruleFile));
		}
		//Generate Modules and all internal Structures
		KieBuilder kieBuilder = kieServices.newKieBuilder(kieFileSystem);
		kieBuilder.buildAll();
		KieModule kieModule = kieBuilder.getKieModule();
		return kieServices.newKieContainer(kieModule.getReleaseId());
	}
}
```

volvemos a ejecutar el test y vemos que todo es exactamente igual, yeah!  Pero vemos que ahora tenemos unos warning que nos indican que el 
nombre de package no se corresponde con el directorio físico y eso no esta mal. Como comenté al principio, **el concepto de package es de "scope"**, pero 
si que tiene razón en que deberiamos mantener un poco coherencia física en lo que estamos definiendo.  
```
File 'src/main/resources/rules' is in folder 'rules' but declares package 'com.demo.product'. It is advised to have a correspondance between package and folder names
```

PACKAGES
--------------------------
Ten en cuenta que al trabajar con packages, 2 rules no pueden tener el mismo nombre, ni 2 funciones tampoco. Además el compilador es coherente y da un error al arrancar
si las definiciones dan conflicto o hay fallos, para evitar posibles "sobreescrituras".

#Inspeccionando el contexto de Drools desde fuera
Imaginemos que tenemos un motor de drools con todas las reglas y procesos bien configurados y nuestra unica finalidad es ponerle una capa por fuera a esa cebolla
y exponer esas funcionadlidad, por ejemplo para que sean usadas desde un API Rest.

Podemos inspeccionar los tipos definidos dentro del motor Drools mediante el api de los distintos elementos del KIEContainer:
```
    	kieContainer.getKieBase().getKiePackage("com.demo.product").getFactTypes(); 
    	kieContainer.getKieBase().getKiePackage("com.demo.product").getFunctionNames();
    	kieContainer.getKieBase().getKiePackage("com.demo.product").getGlobalVariables();
    	kieContainer.getKieBase().getKiePackage("com.demo.product").getRules();
       	etc..
```





Punto de control:
----------------
Bueno, hemos visto:
-Como configurar Drools para usarlo en nuestro SpringBoot
-Como crear rules (rules, LHS, RHS, modify ..
-Condiciones de ejecucion de rules (no-loop, salience
-Como importar y definir funciones drools
-Como importar y definir nuevos tipos dentro del contexto Drools
-Como inyectar objetos del contexto de la JVM como globals para que sean usados.

**Intentar explicar el uso de drools y todas sus variantes en unos simples tutoriales es una osadía. Es pero que este pequeño tutorial te haya ayudado no solo a manejar un poco drools, si no tambien a que pienses como lo puedes llegar a usar en un desarrollo o futuros proyectos.**

Toda la documentacion y el material bélico de de la verison 5 de Drools, la tienes aqui :
https://docs.jboss.org/drools/release/5.2.0.Final/drools-expert-docs/html/



