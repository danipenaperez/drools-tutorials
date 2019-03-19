Introduccion
------------
El concepto de un motor de reglas, difiere del modelo tradicional en el que nuestro codigo invoca a unas funciones o métodos de un servicio pasandole unos objetos de entrada, si no que se basa en que hay objetos que existen en nuestro ecosistema (ejecucion), dichos objetos tienen un estado y hay reglas que defienen que acciones se deben tomar acode a esos estados.

Drools es un framework BRMS (Bussines rule Management System) implementado por la gente de RedHat basado en el algoritmo [Rete](https://es.wikipedia.org/wiki/Algoritmo_Rete). Este algoritmo es capaz de hacer correspondencias entre objetos que entran a un ecosistea y las acciones inferidas correspondientes basado en las reglas definidas dentro de dicho ecosistema.
Rete es capaz de alamacenar dichas correspondecias/emparejamientos, de forma que es capaz de optimizar el procesamiento de un objeto de entrada y las reglas a disparar ahorrando el procesamiento repetitivo que deberíamos hacer en cada ejecución. Su ahorro en recursos computacionales y capacidades de clusterización lo convierten en una herramienta muy óptima para entornos de producción.

Otro punto a su favor, es la encapsulacion de todo el procesamiento funcional de una aplicación. La definición de las reglas de negocio se implementan en un DSL sencillo, de forma que no se requiere un conocimiento tecnico amplio para definir el comportamiento de una aplicación.

El motor de reglas se ejecuta como una "caja negra" portable dentro de nuestra aplicación. De forma que el desacoplo de capas esta garantizado. Estos motores BPM nos permiten susbscribirnos a eventos que sucenden dentro nos proporcionan un api de configuracion para ejecuciones.


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

* **WorkingMemory**: memoria de trabajo, es el contexto de ejecución en el se evaluan y ejecutan las reglas, es decir el runtime.

* **KnowledgeSession**: La sesión se crea dentro de la Working Memory. Esta es la encargada de la preparacion de la ejecución y montar el ecosistema de reglas asociadas a dicha ejecución.

* **KnowledgeBase**: Repositorio donde encontrar las reglas y construir la engine , es decir la Knowledge Session.Como puedes imaginar, ese repositorio puede nutrirse de diferentes fuentes de donde leer la definicion de las reglas: de un directorio local, un repositorio remoto centralizado y versionado de las reglas (, o al fin de alcabo cualquier cosa convertible a un array de bytes ... 

> *El proyecto Business Central Workbench WildFly WAR servido a traves del KIE Execution Server (Wildfly Application server) de la gente de JBoos nos permite desplegar un servidor centralizado de reglas, donde podemos editar, versionar y servir configuracion de una manera centralizada a distintos servidores de ejecucion.*

La forma en como se buscan, indexan , interpretan las definiciones de reglas, el estado de contexto a usar (stateless/statefull), en definitiva la forma en la que el BPM compila y actua es totalmente editable desde el API que nos proporciona jBPM y Drools. 


### KIE (Knowledge Is Everything)
KIE Framework de la gente de RedHat nos proporciona un nivel de abstraccion para ofrecernos las herramientas básicas para el uso del jBPM.


PREPARACION DEL ENTORNO
-----------------------
Podemos importar todas las dependencias desde los repositorios centrales de maven:
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

Para que nos vayamos introduciendo en el manejo de drools, vamos a hacer unos ejemplos muy sencillos donde aprenderemos un poco a definir con unas reglas sencillas y posteriormente intentaremos hacer un ejemplo mas "imaginativo" para que veamos como aplicaríamos drools a un proyecto un poco mas "empresarial".

Este primer ejemplo es muy simple, al contexto de ejecucion de drools le vamos introducir el precio de un producto, este será analizado por las reglas se dispararán una serie de acciones.

## Lo primero **generar la configuracion**
Para instanciar el motor del engine mediante Spring Boot, basta con generar nuestra clase de configuracion usando el archiconocido @Configuration y un metodo que inyecte en el contenedor un Bean KIEContainer. 
Este KIEContainer encapsula todos los elementos que comenté al principio (KnowledgeBase, configuracion Working Memory, etc..) y nos proporcionara un API sencillo para interactuar con todos ellos. 
En este caso lo usaremos para crear la KnowledgeSession cada vez que requiramos una ejecución (cada ejecucion tiene su contexto por lo tanto su runtime, que es configurable en modo stateless o statefull):
```
@Configuration
public class BPMConfigurations {
	//lives on classpath -- src/main/resources/rules/*.drl 
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
Le especificamos que carge 1 fichero de reglas que se encuentran en el classPath (src/main/resources/rules/discountRules.drl) y ya el KIEContainer hace todo por nosotros.


Ahora generamos un POJO para actuar como Fact (ProductPrice.java) muy sencillo (he usado lombok framework para excluir codigo declarativo innecesario): 
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
		priceCalculatorService.executeRules(productPrice);//Call service and internal 
								   //BlackBox rules engine
	}
}
```
Vemos que instanciamos un precio de producto con basePrice = 5. Luego se lo pasamos al service y este ejecuta "la caja negra" del motor de reglas. 
Esta configuracion y ejemplo sencillo es más que suficiente para hacer nuestras pruebas y juguetear un poco.

Vamos a ver el contenido de nuestra definicion de reglas del archivo discountRules.drl. 
Esta primera version es muy sencilla, al introducir el producto en el engine chequeará si el precio del producto es mayor que 2 y si es asi hara un print por consola.

discountRules.drl
------------------
Empezamos con una version muy sencilla:
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

Antes de ver en detalle cada parte, podemos intuir que importa unos tipos de objeto y que hay una rule que tiene una condición y que si se cumple hace un system Out.

Por partes:
* **package** : es una agrupacion lógica de reglas.*No tiene que ver con paqueteria física*. Piensalo más como un namespace, donde  grupos de  elementos tienen relacion (globas, functions y demas cosas quee veremos mas adelante). Los nombres de las rules deben ser únicos dentro de un mismo package (namespace)

* **import** : importamos definiciones de clases que necesitará drools en la compilacion de las reglas y su ejecución. Por defecto indicar que drools importa siempre el paquete java.lang.* ,por lo que podremos usar todas las clases del paquete en nuestras definiciones de reglas.

* **rule** : es el bloque de código que indica inicio y fín de una regla (rule): Como comenté al principio las reglas se componen de 2 partes fundamentales, 
	* when (RHS - Right Hand Side): donde se definen los criterios que la dispararan. 
	* then (LHS - Left Hand Side): donde se definen las acciones.

* **dialect** : el tipo de lenguage usado para las definiciones dentro de las reglas. Los 2 mas extendidos son 
	* **"mvel"**-> (MVFLEX Expression Language): Es un lenguage declarativo mas sencillo y su unica finalidad es hacer el codigo mas legible. Ofrece sintaxis que casa con la nomenclatura java standar. Su uso es casi extendido a la seccion *RHS*.  
	* Hay ya muchos DSL que se basan en esto, pero pongo aqui un ejemplillo de equivalencia de código:
	  	* java version: 
		> $person.getAddresses().get("home").setStreetName("my street");
		* mvel version: 
		> $person.addresses["home"].streetName = "my street";
		
		Mvel también nos permite asignacion de variables en el scope de una rule de manera sencilla ($varName), asi como la deficion de nuevos tipos(classes) de manera sencilla.
	* **"java"**-> Pues Java. Es decir podemos incluir nuestra sintaxis java dentro del .drl.Su única restriccion es que solo se puede usar en el LHS (lefHandSide), es decir en el *then*.



Ejecutando
----------
Si ejecutamos el test, estos son los pasos fundamentales:
* 1. Entra el fact a evaluar con baseprice = 5  desde nuestro código Java
```
ProductPrice productPrice = new ProductPrice(5);//Create the Fact
priceCalculatorService.executeRules(productPrice);//Call service and internal 
                                                   // BlackBox rules engine
```
* 2. El motor de drools comprueba el when y como en este caso se cumple, pues asigna a la variable local $p el objeto ProductPrice. Mvel nos proporciona el acceso al getBasePrice sin necesidad de declararlo.
```
$p : ProductPrice(basePrice > 2 )  // Object({conditions}) 
```
*las condiciones disponibles las vamos a ver mas adelante.. Fact({conditions})*
* 3. Como se cumple ejecuta el then, haciendo el print de la variable seteada $p.
```
System.out.println("EJECUTANDO -Adjust Product Price- para el producto [" + $p + "]");
```

Conditions (Revisando condiciones en LHS - *Left Hand Side*)
----------
Como hemos visto en el paso 2 anterior, se cumple la condicion de que basePrice > 2 (ya que era 5 cuando entro al contexto de ejecucion). 

Para el ejemplo anterior podríamos condiciones mas complejas:

> ( > , < , >=, =<, || , && , == , % , ^, contains, not contains, memberof, not memberof, matches (regExp), not matches (regExp), starswith , etc...) 

Ejemplos:
```
$p : ProductPrice(((basePrice / 5) == 1) && ((basePrice % 5) == 0 ))
```

En vez de anidarlos tambien podemos usar la claúsula por defecto **and** y convertir la condicion anterior en 

```
	when
		$p : ProductPrice((basePrice / 5) == 1))
		$p : ProductPrice((basePrice % 5) == 0 ) //se deben cumplir todas
	then
``` 
Existe una evaluación en modo manual que evalua (**eval({true|false})** a una expresión booleana la ejecución de una regla.
```
	when
		eval(true)
		eval ($p.isZeroPrice()) //por ejemplo link a un metodo booleano interno de la clase
		eval(callMyCustomFunctionThatReturnsABoolean)
	then
``` 
En el segundo eval, dejo ver que Drools como compilador tambien nos permite referenciar a metodos staticos de nuestro codigo o definir funciones dentro del mismo fichero .drl que como ya comente estaran disponibles en todo el package (namespace), esto lo vamos a ver en las siguentes secciones.

Modify{... (Aplicando ordenes en el then RHS )
-----------------------------------------------
Muy bien ya hemos visto las *conditios* pero vamos a ver la órdenes. Hemos visto una sencilla ejecución con el system out, referenciando a la variable local de la rule $p:
```
    then
    	System.out.println("EJECUTANDO -Adjust Product Price- para el producto [" + $p + "]");
```

Si queremos aplicar una modificación vamos a usar el bloque modify. Vamos a bajarle el basePrice un punto, nos quedara asi:

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
*Sencillo verdad?* abrimos un bloque modify con el scope de la variable definida (ya que se cumplio el LHS- *Left Hand Side*) y en el contexto encontramos la función setBasePrice del objeto ProductPrice.

Si haces esta modificación en codigo y ejecutas el Test veras este output:
```
el precio ajustado es 0
```

Rules Attributes (orden de ejecución de las reglas, bucles infinitos, etc...)
-----------------
Como comente antes, el comportamiento en runtime de una rule se puede restringir. Para ello debemos usar los rule atributes que nos ofrece drools. Estos se defienen justo debajo de la definición del nombre de la rule:
```
rule 'rulename'
	//rules Atributtes (availables: no-loop, salience, ruleflow-group, lock-on-active, agenda-group, 
	// activation-group, auto-focus, dialect, date-effective, date-expires, duratio, timer, calendars
	when:
		...
	then:
		...
end
```

Vamos a comentar 2 de ellas (las que me parecen mas genéricas y obligatorias de conocer), si quieres profundizar tienes la referencia oficial [aquí](https://docs.jboss.org/drools/release/5.2.0.Final/drools-expert-docs/html/ch05.html#d0e3761)

# No-loop
Imaginemos que metemos un Fact (ProductPrice.java) que su basePrice = 5 , cuando se cumple la condicion solo le restamos 1 a su basePrice. 

Drools por defecto volvera a evaluar la LHS y verá que sigue cumpliendose así que **la regla será disparada varias veces, hasta que no se cumpla la condicion.**
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
y el Ouput:
```
el precio ajustado es 4
el precio ajustado es 3
el precio ajustado es 2
```

Esto puede ser un quebradero de cabeza en la ejecución, porque si por ejemplo la ejecucion es de tipo void y no se modifica el Fact, entrariamos en un bucle infinito.

Para asegurar que la regla (si se cumple) solo se ejecute una vez, usamos el atributo **no-loop** en la definicion de la regla y  vemos que podemos cambiar el comportamiento de la ejecucion:
```
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
```
Y ahora el Output:
```
el precio ajustado es 4
```

Solo se ha ejecutado 1 vez ya que solo se ha evaluado 1 vez.

# Salience
Por defecto Drools ordena las reglas de ejecución en el orden que se las va encontrando al parsear los fichero de reglas(.drl), de modo que en tiempo de ejecución se ejecutaran las reglas que su RHS se cumpla y por el orden en el que se almacenaron en el motor.

Imagina esta definicion de drl en la que hemos introducido 2 reglas (ya añadimos el no-loop tambien): 
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
y su Output:
```
EJECUTANDO -Adjust Product Price-
EJECUTANDO -Sending Notification-
```

Salience (prominencia) es un sistema de pesos que nos permite indicar prioridades sobre las ejecuciones de las reglas en caso de coincidencia.

Si añadimos el atributo salience a las reglas vemos como podemos especificar el orden:

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
y su Output:
```
EJECUTANDO -Sending Notification-
EJECUTANDO -Adjust Product Price-
```

Ahora se han ejecutado ordenadamente en función del peso(valor) de nuestra rule dentro del engine. Los valores de salience pueden ser negativos si queremos que nuestra regla tenga prioridad mínima y ser lanzada en última instancia.


USANDO EL COMPILADOR
--------------------
Como comenté anteriormente, podemos importar funciones, crear nuevos tipos,  funciones dentro del drl, etc.. 

# functions - > Importando metodos de utilidades:
Pongamos que tenemos una libreria de utilidades, parseos o que realiza cualquier otro tipo de funcionalidad atómica y que nos vendría fenomenal usarlo dentro de nuestras reglas.

Desde Drools podemos importar ese metodo para usarlo dentro de las rules (ten encuenta que ese metodo se expondrá dentro del namespace).

Pues es tan facil como definir el metodo con acceso static en nuestra clase java y luego importarlo en nuestro fichero .drl (recuerda que estara disponible para todo el package):

La Clase Java:
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
```
PrettyTraces -> ***el precio ajustado es 0***
```

> Tambien puedes observar que he metido // para los comentarios

# functions - > Creandolas en drl:
Definir funciones dentro del .drl es muy sencillo. Pero en su declaracion de tipo usamos la palabra reservada **function**, vamos a incrementar el codigo metiendo la definicion tambien:

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
```
PrettyTraces -> ***el precio ajustado es 0***
PrettyTraces -> ***3***
```

Vemos como hacemos la llamada al metodo que acabamos de crear y ademas al prettyTraces que habiamos añadido anteriormente.


# Nuevos Tipos (TYPE - class ) 

Es posible definir nuestras nuevas classes dentro del ecosistema de manera declarativa y sin compilacion previa, ya que el compilador leera la definicion e introducira en el classloader las definiciones para la instanciación.

La declaracion de nuevos tipos se hace en la misma seccion de declaracion de las funciones, usando la palabra reservada "declare".

Por defecto con el uso de "mvel" los getters/setters/toString/equals/hashCode seran añadidos a la definicion.

> El compilador creará 2 constructores por defecto (uno vacio y otro con todos los campos como argumentos).

Si queremos customizar el constructor para usar solo determinados argumentos debemos usar la anotación @key, puedes mas ejemplos en la documentacion oficial [aqui](https://docs.jboss.org/drools/release/5.2.0.Final/drools-expert-docs/html/ch05.html#d0e3418).

```
declare Product
   code : int
   name : String
   description : String
ends
```
Y para su instanciación se puede hacer de esta manera usando sintaxis java (recuerda que esta sintaxis solo es aceptada en RHS):

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

```
PrettyTraces -> ***el precio ajustado es 0***
PrettyTraces -> ***3***
PrettyTraces -> ***Product( code=3321, name=Leche, description=Rica en Calcio )***

```

> Como podemos observar estamos "uglificando" el codigo en este archivo .drl ya que se nos empieza a ir de las manos. Entonces empieza a tener sentido el concepto de "package" , porque podemos tener varios ficheros .drl (uno con definicione de tipos, otro con funciones y otro con las rules...) y al compartir package (namespace) estaran disponibles de manera "global" dentro del namespace.

En la siguiente seccion veremos como incluir varios ficheros , pero antes vamos a ver los "Global" que va muy relacionado con lo que acabamos de ver.

# Importar objects desde el contexto java (globals)

Hemos visto ya que podemos :
* Definir nuestras funciones (function)
* Definir nuestros tipos (classes)
* Importar tipos para usarlos en nuestros .drl (import pck.subpck1.subpck2.className )

Y ahora vamos a ver como meter objetos ya instanciados y disponibles en la maquina virtual al contexto de ejecución de Drools.

Puede ser muy util injectar un bean de servicio de nuestro contexto de Spring para que sea utilizado en las LHS para realizar operaciones complejas (piensa en insertar un DAO o cualquier tipo de Servicio).

A este tipo de inyecciones se las denomina **globals**. Para estas si que necesitamos editar nuestro test Java ya que se lo pasamos como argumento en la construccion de la session de ejecución.

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
Ahora nos interesa tener este objeto dentro del contexto de ejecucion de las rules de drools, asi que lo inyectamos como global cuando solicitamos la ejecucion.

La clase PriceCalculatorService.java (la del principio, acuerdate!) llevamos un buen rato sin tocarla y en mi opinion esto es lo bueno de drools, que solo montamos los "hierros" en java y lo delegamos todo en el motor de reglas que debería actuar como una caja negra para nosotros. 

Modificamos el método para añadir un "globals" al conexto de ejecution.Para ello usamosa el API que nos proporciona la KieSession:
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
He inyectado el servicio a traves del global name "publishTool" y lo referencio dentro del .drl en la parte de definiciones que estabamos usando para las functions y los tipos que vimos anteriormente.
```
global com.dppware.droolsDemo.services.PushSubService publishTool;
```
y lo uso en alguna RHS (then) para comprobar que podemos usarlo:
```
publishTool.publishNewProductCreated(pro); 
```

El archivo .drl ahora tiene esta forma, vemos que metemos la ultima linea de ejecucion, en la que usamos el global para publicar:

```
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
```
PrettyTraces -> ***el precio ajustado es 0***
PrettyTraces -> ***3***
PrettyTraces -> ***Product( code=3321, name=Leche, description=Rica en Calcio )***
Publishing newProduct Topic , content [{"code":3321,"name":"Leche","description":"Rica en Calcio"}]
```

ORGANIZANDO EL CODIGO
---------------------
Hemos visto ya que tenemos un batiburrillo de declaraciones, imports, functions, declares, tipos, globals, rules...
y todo en el mismo archivo...bufff es una casa de locos. 

Vamos a estructurarlo un poco, ya no solo por tenerlo mas legible, si no porque si sabemos dividir bien, podemos generar "ecosistemas" solamente juntando piezas (.drl) y lograr asi comportamientos portables y flexibes.

La idea de dividir en archivos, separando las reglas en un .drl, las definiciones de tipos en otro, las funciones en otro, etc.. 
nos va a generar una dependencia entre imports de ficheros , todavia no hay un sistema de dependencias entre 
ficheros de reglas y lo deberiamos de gestionar nosotros a mano :-( . 

Por otro lado estructurar un poco el .drl tambien creo que es una buena practica y  nos ayuda a mantener la atomicidad y coherencia del contexto de las reglas.

Yo voy a dividir en distintos archivos, para que se vea y entienda el concepto de package que comente al princpio y que no he "implementado/explicado" todavía por darle continuidad a este tutorial.

Tambien, habrá de los que piensen que meter los "global" crean una dependencia de ejecucion con elementos externos , totalmente de acuerdo. Tambien podríamos optar por importar tipos del classpath y hacerlo todo en drools, pero con la facilidad que nos da Spring para un monton de cosas pues eso...para gustos los colores.

Como hemos visto, importar los archivos .drl en nuestro ecosistema, se reduce a importar un array de bytes, asi que podríamos optar por implementar un repositorio  central de ficheros .drl , versionable y que a parte nos provea de informacion de dependencias de archivos .drl... no sé, deja volar tu imaginación..quizas un springcloudConfig /zookeeper, etc.. 

## Sacando la tijera
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

# product_dependencies.drl
Solo contiene informacion acerca de agentes externos (globals requeridos)
```
package com.demo.product;

//global Sets
global com.dppware.droolsDemo.services.PushSubService publishTool;

dialect  "mvel"
```

# product_functions.drl
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
 
# product_rules.drl
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

Volvemos a ejecutar el test y vemos que todo es exactamente igual, yeah!  Pero vemos que ahora tenemos unos warning que nos indican que el nombre de package no se corresponde con el directorio físico y eso no esta mal. Como comenté al principio, **el concepto de package es de "scope"**, pero si que tiene razón en que deberiamos mantener un poco coherencia física en lo que estamos definiendo.  

```
File 'src/main/resources/rules' is in folder 'rules' but declares package 'com.demo.product'. It is advised to have a correspondance between package and folder names
```

PACKAGES
--------------------------
Ten en cuenta que al trabajar con packages, 2 rules no pueden tener el mismo nombre, ni 2 funciones tampoco. Además el compilador es coherente y da un error al arrancar si las definiciones dan conflicto o hay fallos, para evitar posibles "sobreescrituras".

# Inspeccionando el contexto de Drools desde fuera

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

> **Intentar explicar el uso de drools y todas sus variantes en unos simples tutoriales es una osadía. Es pero que este pequeño tutorial te haya ayudado no solo a manejar un poco drools, si no tambien a que pienses como lo puedes llegar a usar en un desarrollo o futuros proyectos.**

Toda la documentacion y el material bélico de de la verison 5 de Drools, la tienes aqui :
https://docs.jboss.org/drools/release/5.2.0.Final/drools-expert-docs/html/

Ls reglas de drools las podemos encapsular en .jar y manejar sus dependencias con Maven

Usando el API
----------------
El KIEContainer nos provee un API muy rico para analizar, crear contextos, leer definiciones de objetos y un largo etc...
```
kieContainer.getKieBase().getKiePackage("com.demo.product").getFactTypes(); 
    	kieContainer.getKieBase().getKiePackage("com.demo.product").getFunctionNames();
    	kieContainer.getKieBase().getKiePackage("com.demo.product").getGlobalVariables();
    	kieContainer.getKieBase().getKiePackage("com.demo.product").getRules();
    	kieContainer.getKieSessionConfiguration();
    	kieContainer.newKieSession();
    	kieContainer.newStatelessKieSession(conf);
    	kieContainer.newRuleUnitExecutor();//informacion acerca del threadpool
    	y un largo etc..getClass().
```
Y muchos listeners para observar que esta pasando:
```
kieContainer.newKieSession().addEventListener(ProcessEventListner );;
kieSession.addEventListener(new RuleRuntimeEventListener() {
			
			@Override
			public void objectUpdated(ObjectUpdatedEvent event) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void objectInserted(ObjectInsertedEvent event) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void objectDeleted(ObjectDeletedEvent event) {
				// TODO Auto-generated method stub
				
			}
		});
etc..
```

> A veces nos interesa por performance crear una session que no contenga todas las reglas, porque si te paras a pensarlo, se evaluan todas las reglas cada vez que es disparada la ejecucion y en muchos casos eso puede no tener sentido con la consecuente degradacion de rendimiento. **El tunning de Drools deberias manejarlo desde esta API de KIEContainer.**



