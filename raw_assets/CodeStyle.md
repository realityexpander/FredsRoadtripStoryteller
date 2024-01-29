# Code Style based on BOOP principles

## Introduction & Design Goals

- Create a sample application with a pure BOOP domain that adheres to Alan Kay's and Yegor Bugayenko's OOP styles
- Document the design decisions and rules that were made to create the sample application.
  - BOOP stands for "Back-to Object Oriented Programming" or "Bugayenko Object Oriented Programming"
  - Writing code that is easy to change & comprehend quickly using English prose.
  - Back to the original OO conceptual basics approach for Java coding style in the Domain layer for Role objects.
  - Built to allow any Role object to be easily separated into an independently horizontally scalable entity. (ie: microservice)

### Inspiration

- BOOP is a design pattern that is inspired by:
  - Alan Kay's OO style & lectures, HyperCard, the ideas behind Smalltalk.
  - Yegor Bugayenko's lecture series on OOP and book Elegant Objects.
  - David West, PhD's book "Object Thinking"

<i>"The key in making great and growable systems is much more to design how its modules communicate rather than what<br> 
 their internal properties and behaviors should be." –– <b>Alan Kay</b></i>

## Contents

- [Developer Experience is Paramount](#developer-experience-is-paramount)
- [Avoid Ugly COP Paradigms](#avoid-ugly-cop-paradigms)
- [Some Useful Design Choice Departures from Strict BOOP](#some-useful-design-choice-departures-from-strict-boop)
- Based on my Library App, which is a simple CRUD app for a Library. Many of the same concepts apply to this app.
  
### Code Style & Rules

- [Code Style](#code-style)
- [A Note on Managing Source-Code Line Complexity](#a-note-on-managing-source-code-line-complexity)
- [A Note on Managing Source-Code Line-Complexity](#a-note-on-managing-source-code-line-complexity)
- [Encapsulation of Data via Intention-named methods](#encapsulation-of-data-via-intention-named-methods)
- [No `null` in Domain](#no-null-in-domain)
- [Intention Revealing Error Messages](#intention-revealing-error-messages)
- [No Shared Global State](#no-shared-global-state)
- [No Dependency Injection Framework](#no-dependency-injection-framework)
- [Constructor Convenience](#constructor-convenience)
- [Anti-inheritance](#anti-inheritance)
- [Shallow Hierarchies](#shallow-hierarchies)
- [No Static Methods](#no-static-methods)
- [Dumb Container Objects for Data Transfer Only to/from Domain](#dumb-container-objects-for-data-transfer-only-tofrom-domain)
- [Extremely Limit use of `Else` blocks](#extremely-limit-use-of-else-blocks)
- [No `Void` Methods](#no-void-methods)
- [No `Null` Checks](#no-null-checks)
- [Synchronous Code](#synchronous-code)
- [Encourage Explicit Boolean Naming](#encourage-explicit-boolean-naming)
- [Encourage Variable Naming with Explicit Types](#encourage-variable-naming-with-explicit-types)
- [Encourage Explicit Naming of Any Overloaded Term](#encourage-explicit-naming-of-any-overloaded-term)
- [Use Result Object for Errors & Exceptions](#use-result-object-for-errors--exceptions)
- [Avoid C++/Java Design Pattern Hacks](#avoid-cjava-design-pattern-hacks)
- [Prefer Use of Early Return](#prefer-use-of-early-return)
- [Single Responsibility of Role](#single-responsibility-of-role)
- [Reverse-scope-naming Style](#reverse-scope-naming-style)
- [Naming of "Inverse" methods](#naming-of-inverse-methods) 
- [Explicit Naming of "Transfer" methods](#explicit-naming-of-transfer-methods)
- [Explicit Naming of "Find" methods](#explicit-naming-of-find-methods)
- [Explicit Naming of "Maps" and "Lists"](#explicit-naming-of-maps-and-lists)
- [Guard Clauses](#guard-clauses)
- [Domain Role Object can create other Domain Role Objects](#domain-role-object-can-create-other-domain-role-objects)
- [Minimal Annotations](#minimal-annotations)
- [Acceptable Acronyms, Prefixes, and Suffixes](#acceptable-acronyms-prefixes-and-suffixes)

### Developer Experience is Paramount

- Quick comprehension of code is paramount, code should exude intention.
- Write code in a way that is oriented to the human reader (not computer), as code is read 100x more than it is written,
  and computers really don't care what the code looks like.
- The developer experience is paramount, and should be the primary focus of the design.
- Architected by layer, and each layer is grouped by feature
- Allows convenient and easy to comprehend navigation of the code.
  - One downside is the hierarchy is separated into different folders, so it's not obvious what the class hierarchy is.
  - To remedy this, documentation about the data hierarchy should exist near the code (maybe a README). 
- Built to test from start to finish, with no external dependencies.
- Everything is fake-able (mock-able) and isolated for ease and speed of testing.

### Avoid Ugly COP Paradigms

Class Oriented Programming (COP) is a style of programming that seems to be primarily focused 
around continuing to use old procedural/imperative styles leftover from C and C++, but with
Class "wrappers" instead of just data structures, files and functions (procedures) like in C.
Much of what most people call OOP is actually just COP, and is not actually OOP at all. It's important
to know the difference, because the two styles are very different, and have very different
advantages and disadvantages.

- BOOP seeks to entirely <b>avoid</b> the COP (Class Oriented Programming) paradigms & idioms, such as:<br>
  - Using Classes as dumb data containers, with no methods or minimal methods.
  - Using Classes as name space for static methods, with no associated data.
  - Using static methods to modify object data directly.
  - Using static methods and static variables to avoid having to create objects.
  - Exposing internal data structures and mutable objects.
  - Allowing `null` to be returned from methods.
  - `Null` checks everywhere.
  - Allowing multiple shared access to static global variables/state.
  - Over use of inheritance, and deep inheritance hierarchies.
  - Many "Gang of Four Design Pattern" implementations to solve language design problems that should not exist in 
    the first place.
  - Factories, Builders, AbstractFactoryFactories, AbstractFactoryBuilderFactory and other hacky "creational" patterns.
 
### Some Useful Design Choice Departures from Strict BOOP

- Use of a Model and DTO/Entity layer to separate the Domain layer from the Data layer and manage `Info` data transfer.
- Some constructors use code for validation and importing JSON and will throw exceptions when not valid. These are the exceptional cases and not normal happy path.
- Some constructors accept `null` values to indicate “use a default value here” but not used anywhere else in the App. All values passed around the Domain layer must be non-null.
- I had to make `id` of objects public and mutable to work with Gson json importing, I don’t know a workaround for it.
- I had to do some java reflection and casting in the data layer and to handle type-safe UUID’s.
- Use of Early Return for error conditions, and to avoid deep nesting of code.

## Code Style

- Prevent <b>"Whats this for?"</b> and <b>"What does that do?"</b> questions by using explicit 
  intention-revealing names, leaning towards pedantic and patronizing, for everything.
- Prefer verbosity of descriptions to brevity of code. Should always be conveying intent as dense as possible 
  but still readable English.
- Risk pedantic naming over brevity of code. Strive to convey meaning as densely as possible, 
  but not at the expense of clarity.
  - You may think you know what a variable/method is for, but the next person may not.
  - Yes, this risks job security, but it also makes it easier to change code as you keep.
  - If you think someone will be confused by something, take extra time choosing names and add the minimum number 
    of comments to explain <b>WHY?</b> This is also the value of pair programming, someone else can ask you why 
    you did something. Instead of just telling them, that's a time to find better names, or refactor the code to 
    make it clearer, or add a "why?" comment to clarify.
- Even this short guide repeats ideas to make it easier to understand what is important and what is not.
- Strive to make code read like regular English as possible, and to be able to understand it without 
  using IDE tools (like cursor-hover to find var types).
    extending the code base. We risk improving the developer experience for our own sake.
- Limit language to plain-old Java 8
- Strive to write Domain layer code in plain-old idiomatic Java as much as possible, and read like English prose.
- A person who doesn't code should be able to look at a method or variable and know what it does/means.
- Some of these ideas are contradictory, and those are the ones that require more thought and consideration for the situation.

### A Note on Managing Source-Code Line-Complexity

- Humans can handle 7±2 items in their short-term working memory at a given time. Limiting the number of items
  allows the reader to comprehend what is written faster and easier.
- Limiting the complexity of each line of code to 6 items improves the reader's speed and ease of comprehension.
- Consider breaking up into multiple lines when nearing 6 items on a given line.
- Line complexity should never have more than 9 items on a given line.
- Strive to keep line complexity to maximum of 6 items, and know when the line complexity exceeds that number, it 
  causes a dramatic increase in the cognitive load which slows comprehension for most readers.

### Encapsulation of Data via Intention-named methods

  - Set and Get methods are not used, instead methods are named for their intention.
  - Problem: The English word `set` and `get` are _extremely_ generic 
    - Hundreds of definitions, each with many subtle different meanings, based on context.
    - `set` and `get` do not reveal the underlying intention of the method. 
    - It's a very convenient shorthand for the code-writer and always confusing for the code-reader.<br> 
      Requires investigation into what is actually going on, specifically network, CPU or disk access.
- No setters
  - All changes must be made via intention-named methods.
  - Only immutable copies of objects are returned, for read-only purposes.
- No Direct Reference Getters 
  - No references to internal mutable objects (all fields are `final`)
  - Only return copies of information.
  - Never reveal internal structures—always return _curated_ copies for a specific intended purpose.
  - Returning/Exposing `Role` objects or `id's` is OK.
    - `Role` objects are immutable and contain references to their mutable `Info` data.
    - `id` is immutable, and is used to fetch Info objects from their respective Repositories.
  - Should never return `null`
    - Return an intention-revealing object instead
    - Prefer `Result` or `Empty` object instead of `null`
    - `boolean` is acceptable, over `null`.
- Methods that require network, disk access or CPU time should be labeled with that intent.
  - Prefer `calculateTotalCost()` over `getTotalCost()`
  - Prefer `fetchInfo()` over `getInfo()`
  - Prefer `findUserIdOfCheckedOutBook` over `getCheckedOutBookUserId()`
- When the method is a simple data-accessor that just returns a simple field or object, no need to use prefix `get`.
  - Prefer `id()` over `getId()`
  - Prefer  `info()` over `getInfo()`
  - Prefer  `sourceLibrary()` over `getSourceLibrary()`

### No `null` in Domain

- `null` only allowed to be passed in constructors
  - used to indicate <i>"use a reasonable default value for this parameter"</i>
- `null` is checked for in constructors only, usually to create a reasonable default value.
- `null` is not allowed to be returned from methods in the Domain.
  - Prefer to `return` "Empty" or `Result` objects instead of `null`.
- Use intention-named objects that indicate the reason for a `null` case.
  - ie: `PrivateLibrary` instead of a `null` for an "unknown" system Library object.
  - This can often become awkward to describe in English, so the use of `null` must always be questioned.

- <b>Important Exception:</b>
  - `null` is returned from `fetchInfo()` when no error has occurred. 
  - If there is an error, it is returned in a `Result` object.
  - This is for convenience: 
    - It allows the `fetchInfo()` and error handling to be a single line.
  - `null` still used outside of Domain, but prefer to limit its use in general.

### Intention Revealing Error Messages

- Error messages should be human-readable, clearly reveal the issue encountered.
- include `id` of associated object(s) in the message or useful data for the issue.
- This is to prevent guessing and hunting what the cause of the issue may be.
- The unhappy path is the more complex path, so doing this helps reduce its complexity.

### No Shared Global State

- No shared mutable state
- No static/global variables
- No global accessing state of App (except via a passed-in Context object)

### No Dependency Injection Framework

- All dependencies passed in constructors
- Singleton objects reside in the Context object, and are passed in constructors.

### All Objects Immutable
- All objects are immutable, except for the referred `Info` objects which is only modified via `Role` methods.
  - Important exception:
    - The `UUID2` `id` and `UUIDType` values for all objects are kept mutable due to limitations of java:
    - These are mutable because of how JSON imports work. 
      - The `id` must first be extracted from the JSON data before the new Object is created. 
      - The `UUIDType` is mutable because it is not known at object creation time, and must be set after the JSON is parsed.
      - This is a known limitation, and I am unaware of a workaround that doesn't involve a lot of complexities.
      - We have to keep `_id` public, and prefer to have it accessed via public `id()` method.
      - The `_id` must be kept public for the `Gson` deserialization... if there is another way to do this, I would like to know.
      - The setter function is public but noted with a `_` prefix to indicate its special case.
- `Role` objects are immutable and communicate or contain other Role objects.
- `Role` objects contain references to their mutable data (Info) which is updated or fetched automatically when the
  role object is updated. 
  - This data is not exposed directly, only through `Role` or `DomainInfo` methods.
- *Yes, things are copied.* 
  - This is easier than dealing with the complexity of mutable objects.
  - It is easier than working with threads.
  - Optimizations can be made later if needed, the architecture is designed to easily allow for this.

### Constructor Convenience

- All dependencies passed in constructor
  - No Dependency Injection framework (_does anyone want to google a thermosiphon?..._)
- Many different constructors included for many different ways to create objects
- Singletons passed in constructor, held in the Context object
- No `null` objects
  - `null` checks on the input to the constructor for special case constructor handling 
  - `Null` is normally intended to generate reasonable default values
- No thrown exceptions except for actual errors that are not expected.
  - ie: `User` object contains a `UserInfo` object, which is updated automatically when `User` is updated
- Constructors have one primary entrypoint, and all other constructors call this one.
  - the only exception is for JSON and Info constructors, since they use special types.

### Anti-inheritance

- Minimal & shallow use of inheritance
  - <code>Model ➤➤ {Domain} ➤➤ {Entity}{Domain}Info</code> for the `Info` objects inside each `Domain` Object.
    - ie: <code>Model.DTOInfo.DTOBookInfo</code>
    - ie: <code>Model.DomainInfo.BookInfo</code> 
      - note:`BookInfo` is <i>not</i> a `DomainBookInfo` because the Domain is the core and more plain java-like, 
        so we use the simplest name for it.
  - <code>IRepo ➤➤ Repo ➤➤ {Domain}Repo</code> for the `Repo` objects
    - ie: <code>Repo.BookInfoRepo</code> 
  - <code>Role ➤➤ {DomainRole}</code> for the `Role` objects
    - ie: <code>Role.Book</code>
- Minimal use of Interfaces
  - only where needed for testing via fakes/mocks 
- One abstract class to define the `Role` Object class
- Be very wary of any attempts to `generic-ify` using `abstract class`. 
  - Prefer extending concrete classes or Duplicating code at least 3 times
  - Or find yourself having to make the same change in multiple places too often
  - Prefer comprehension over "lets make this generic, but add special cases"
- Model.Domain.Entity class should be 3 levels MAX, unless a very unusual case. 
  - You should be able to keep it as flat as possible
    - use packages to put the objects in the appropriate places, usually together with the feature.
    - Keep the Class Inheritance simple, and allow the package arrangement can be complex.

### Shallow Hierarchies

- Keep hierarchies as flat as possible, because deep hierarchies are difficult to understand and change.
- If reasonable parameterized behavior can be captured in a `Role`, it is preferred over creating 2 or more classes.
  - example:
    - [Library ➤➤ PrivateLibrary with `isOrphan` flag] &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;    ⬅︎ shallow is preferred
    - vs
    - [Library ➤➤ PrivateLibrary ➤➤ OrphanPrivateLibrary]
  - prefer the shallower hierarchy with the `isOrphan` flag.
  
### No `Static` Methods

- Use of `Static` methods is severely limited to only those that are:
  - Pure functions ie: have no side effects that change data outside the function.
  - Used to create objects from JSON, XML, another object, the network, etc.
  - No modification or creation of global state
  - No modification of any state outside the function

### Dumb-Container-Objects for Data Transfer Only to/from Domain

- Dumb Container objects (`InfoDTO`, `InfoEntity`) are immutable and only used to pass data to/from outside domain
  to domain `Role` objects.
- Note: DTOs and Entities are still useful to maintain separation of concerns and to communicate with
  the world outside domain, and allows independent changing and versioning of the domain/DTO/Entity objects.

### Extremely Limit use of `Else` blocks

  - Code for conditions checks first and `return`s early if the condition is not met.
  - A final `return` is always the "happy path" success `return` (unless for rare exceptional cases.)
  - Only in rare, exceptional rare cases resort to using `else` blocks.
    - Always ask if it can be written in a way that doesn't use `else`.
    - Maybe split into two functions?

### No `Void` Methods

  - All methods return something, even if it's just a `Boolean` or `Result` object.
  - Exception: In UI layer, nullable values for error display are acceptable.

### No `Null` Checks

  - Avoid if any way possible any `null` checks in code
  - Avoid `!` as this will cause a crash if the value is `null` at runtime.
  - Exception: In UI layer, nullable values for error display are acceptable.

### Synchronous Code

  - Keep code as synchronous as possible, or looking synchronous.
  - If callbacks are needed, they should be wrapped to look synchronous.

### Encourage Explicit Boolean Naming

- Boolean variables and methods are named explicitly
  - Use of `is{something}` or `has{something}` is preferred over `!` operator
  - Always name the variable for the positive case
  - `is{something}`
  - `has{something}`
  - `should{something}` - use sparingly for parameters, consider using `enum` instead.
  - Avoid using `!` (boolean NOT) operator, if possible.
    - OK to use `isNot{something}` over the `!` operator if it makes the code more readable.
    - Avoid blindly creating `isNot{something}` or `hasNot{something}` just to oppose each positive 
      case, only create a "negative case" method when it is needed, not just automatically.
  - Attempt to convey intent
    - ie: `isPrivate` is preferred over `isNotPublic`
    - ie: `hasFines` is preferred over `isBalanceOverZero`

### Encourage Variable Naming with Explicit Types

  - Slight nod to Hungarian Notation, it is still useful for readability in limited cases.
  - The emphasis on reading without IDE assistance is important, and explicit type naming helps with this.
  - `{Domain}Id` vs `{Domain}` Types
      - Parameter names are explicit about if they are `{Domain}id` or `{Domain}` objects
    - `Id` 
      - Appending `Id` to the end of a UUID2<> type variable name is acceptable and encouraged
      - ie: `userId` is preferred over `user` or  plain `id`
    - Domain
      - If the object is a `Domain` object, then the name should be `{Domain}` and not `{Domain}Id`
      - ie: `user` is preferred over `userId` in this case.
  - `Info` vs `Role` Types
    - Parameter names are explicit about whether they are `Info` or `Role` objects
    - Appending `Info` to the end of the parameter name is acceptable and encouraged
      - ie: `userInfo` is preferred over `user` or plain `info`
    - Using the plain `{Domain}` name is preferred if the object is a domain `Role` object
      - ie: `user` is preferred over `userInfo` in this case.
  - Unit-free types 
    - `Double`, `Float`, `Integer`, `Long` types should never be used raw, and should always have a unit attached.
      - ie: `maxAcceptedPennies` is preferred over `maxAccepted`
      - ie: `MaxTrialTimeEpochMillis` is preferred over `MaxTrialTime`

### Encourage Explicit Naming of Any Overloaded Term

- If you must use an overloaded generic terms (like `set` or `get`), always find a name that is very specific.
- ie: `setBookInfo` is preferred over `setInfo`
- ie: `getUUID2TypeStr` is preferred over `getType`

### Use Result Object for Errors & Exceptions

- Use of `Result` object to return success or failure
  - Encapsulate the error message in an `Exception` object.
  - Use instead of throwing an `Exception`, return a `Result` object with the error message and `Exception`.
- Avoid returning null or raw values
  - Use `Result` object to return success or failure
  - Encapsulate the error message in an `Exception` object.
  - Use instead of throwing an `Exception`, return a `Result` object with the error message and `Exception`.

### Avoid C++/Java Design Pattern Hacks

- Java has inherited many bad ideas from C, C++ and other languages. 
- Many of the ideas were so bad that a common set of workarounds were created and passed around the community 
  (or discovered independently). These eventually became "industry standard" which slowly turned into "best practices."
- These were then catalogued in many books, sold in lectures and conferences, made into clever repeatable acronyms,
  and eventually became a "gospel holy truth" and assumed "just the way it's done", even though it was often taught
  without any explanation of why it was done that way, or proof any of it works optimally, or even works at all.
- Turns out many of the patterns were after-thought workarounds to fundamental language design flaws, directly
  inherited from C++ and C (and other languages) that never were resolved properly much less questioned.
- We know this now because recent language versions have remediated <i>some</i> of these issues, and other languages 
  like Kotlin illustrate how to address these flaws in a more consistent, comprehensible and maintainable manner. 
- Combined with BOOP, many of the popular design patterns just don't make sense and add unnecessary 
  complexity and confusion. <i>But it did pay a lot of presenters' and authors bills for a long time!</i>

#### Specific Examples of Bad Design Patterns

- No Factory patterns
  - Just use constructors. 
- No Builder patterns
  - Create a new object modified copy from the old object, and return the new object. 
  - No need for a builder. 
  - Use `.with{someField}(updatedValue)` method to update `someField` member field.
- No Fluent interfaces
  - Use `.with{someField}(updatedValue)` to update each `someField` member field.
- Interestingly, limited use of the hated `Singleton` and `Repository` patterns do actually fit well with BOOP 
  and are encouraged. <i>Even a broken clock is right twice a day!</i>

### Prefer Use of Early Return

- Multiple early `returns` for ease of error handling 
  - Unhappy path errors `return` immediately
- One success `return` at the end is preferred.
- Note: multiple Success `returns` are acceptable, but discouraged. 
  - Maybe break up into 2 functions?

### Single Responsibility of Role

- BOOP makes clear separation of concerns easy and understandable.
- Each Role has a single responsibility, and only handles that responsibility, and delegates all other responsibilities
  to other Roles.
- Each Role has many methods to handle its responsibility, and return encapsulated intention-revealing data to 
  other Roles.
- No direct access to any other Role's data, all data is encapsulated and only accessed through methods.
- All Role Info is returned as copies, never direct references.
  - This makes it possible to have a Role change independently of other Role objects. By defining communication
    protocols via methods.
- This architecture will update the info for the domain object automatically when the Role object Info field is updated.
- No need for a separate `update()` method, in most cases.

### Reverse-scope-naming Style

- Starts with the most specific adjective to more general adjectives, and ends with the name of the actual concrete type.
- Domain objects are the plainest named.
  - ie: `User` and `Account`
- Subtypes are always given an adjective name that differentiates it
  - ie: `Library` and `PrivateLibary`
- If it's a generic item, adding a descriptor is encouraged.
  - ie: `accountStatus` is preferred over `status`
  - ie: `currentFine` is preferred over `fine`
- Concrete item is at the end of the name
  - ie: `maxAcceptedPennies` instead of `maxPenniesAccepted`
    - we want to refer to the  `Pennies` not `Accepted`s (whatever those are!)
  - ie: `accountAuditLog` vs `log`
    - We know it's a Log. 
    - What kind of log? An Audit log. 
    - What kind of audit log? An Account Audit Log. 
- It is acceptable and preferred to chain more precise adjectives in the name first and 
  move to more general adjectives.
  - ie: `OrphanPrivateLibrary` is preferred over `Orphan`
  - ie: `updatedAccountStatus` is preferred over `updated` or `status`

### Naming of "Inverse" methods

- Prefer using the same verb and a short adjective modifier than to use two different verbs for inverse/opposite methods.
- ie: Prefer `CheckIn` and `CheckOut` to `Borrow` and `Return`
- ie: Prefer `Register` and `UnRegister` to `register` and `delete` (or `remove`)
- ie: Prefer `Suspend` and `UnSuspend` to `suspend` and `reinstate`
- ie: Prefer `Activate` and `DeActivate` to `activate` and `suspend` (or `disable`)
  - Exceptions:
  - For CRUD operations, it is acceptable to use standard opposite terms: `create`, `add`, `insert`, `delete`
  - `Close` and `Open` are preferred over `Open` and `UnOpen` (unless the domain specifies it)
  - `Push` and `Pop` are preferred over `Push` and `UnPush` (unless the domain specifies it)
  - `Put` and `Get` are for primitive `Map`-like operations only.

### Explicit Naming of "Transfer" methods

- Use of `From` and `To` encouraged, to show explicit intent.
  - ie: `checkOutBookToUser` is preferred over `checkOut` or `checkOutBook`
  - ie: `transferBookSourceLibraryToThisLibrary` is preferred over `transferBook`
    - yes, it's wordier, but leaves no doubt as to what is going on. 
- Use of `By` if there is an authorization, or a delegate.
  - ie: `activateAccountByStaff` is preferred over `staffActivateAccount`
  - ie: `findAllCheckedOutBooksByUserId` is preferred over `findAllUserIdCheckedOutBooks`
    - even though both convey the same meaning, one is easier to comprehend in English.

### Explicit Naming of "Find" methods

- For "search" functions use the word `find` in the name.
  - ie: `findCheckedOutBooks` is preferred over `searchForCheckedOutBooks`
- Use of `By` is encouraged
  - ie: `findCheckedOutBooksByUserId` is preferred over `findCheckedOutBooks`
  - ie: `findCheckedOutBooksByUserIdAndLibraryId` is preferred over `findCheckedOutBooks`
    - even though both convey the same meaning, one is easier to read in English.
- Use of `Of` is encouraged
 - ie: `findUserIdOfCheckedOutBook` instead of `findCheckedOutBookUserId`
 - even though both convey the same meaning, one is easier to read in English.

### Explicit Naming of "Maps" and "Lists"

- List the `from` type and the `to` type in the name of the map.
- It is preferred to use `To` between the `from` and `to` types.
- It is preferred to add `Map` or `List` at the end of the variable names.
- ie: `acceptedBookIdToSourceLibraryIdMap` is preferred over `acceptedBooks`
- ie: `timeStampToAccountAuditLogItemMap` is preferred over `auditLog`
- This makes the JSON data easy to read and understand out of context.
- `List` can refer to Arrays or a "single column" data.
- `Map` can refer to any Map or "two column lookup" data.

### Guard Clauses

- Guard clauses are used to check for errors and return early if error is found.
- Basic data validation

### Domain Role Object can create other Domain Role Objects

- This is acceptable for Domain objects!
  - They all can instantiate themselves & others.
  - Others can instantiate them.
  - Their `Info` gets pulled in from their respective `Info` Repository on demand with a call to `info()`.
  - `Role` objects are essentially smart pointers to their `Info` objects & other `Role` objects.

### Minimal Annotations

- Annotations are used sparingly, and only for the most important things, like @NotNull, @Override, @Suppress, etc.

### Acceptable Acronyms, Prefixes, and Suffixes

<table>
  <tr>
    <th>Acronym</th>
    <th>Preferred Use</th>
  </tr>
  <tr>
    <td> 
      <code>ctx</code>
    </td>
    <td>
      Acceptable to use in place of <code>context</code>.
    </td>
  </tr>
  <tr>
    <td> 
      <code>Id</code>
    </td>
    <td>
      for identifiers, ie: <code>userId</code>, <code>bookId</code>, <code>libraryId</code>, etc.
    </td>
  </tr>
  <tr>
    <td> 
      <code>Info</code>
    </td>
    <td>
      For Classes that contain the Info for the <code>Role</code> Class internal information.
    </td>
  </tr>
  <tr>
    <td> 
      <code>Repo</code>
    </td>
    <td>
      For Repository Classes.
    </td>
  </tr>
  <tr>
    <td> 
      <code>DTO</code>
    </td>
    <td>
      Prefix for "Data Transfer Object" classes.
    </td>
  </tr>
  <tr>
    <td> 
      <code>num</code>
    </td>
    <td>
      Prefix for counts or amounts, or other integer intentions that are sum-like.
    </td>
  </tr>
  <tr>
    <td> 
      <code>max</code> & <code>min</code>
    </td>
    <td>
      Prefix for limits on ranges.
    </td>
  </tr>
  <tr>
    <td> 
      <code>cur</code>
    </td>
    <td>
      Prefix for <code>current</code> is gray area.<br>
      • Prefer spelling out unless too pedantic for a local context.<br>
      • Indicates the current value for the object.
    </td>
  </tr>
  <tr>
    <td> 
      <code>Amt</code> & <code>Amnt</code>
    </td>
    <td>
      These are in a gray area.<br>
      • Prefer spelling it out, ie: <code>Amount</code>.
    </td>
  </tr>
  <tr>
    <td> 
      <code>Ct</code> & <code>Cnt</code>
    </td>
    <td>
      These are too vague and one is mildly rude in English.<br>
      • Prefer spelling it out, ie: <code>Count</code>.
    </td>
  </tr>
  <tr>
    <td> 
      <code>Kind</code>
    </td>
    <td>
      Use in <code>enums</code> instead of the word <code>Type</code> which is 
      reserved specifically for the clazz <code>Class&lt;?&gt;</code> types.
    </td>
  </tr>
  <tr>
    <td> 
      <code>Str</code>
    </td>
    <td>
      • Append to a string variable name that represents a specific type.<br>
      • ie: <code>UUID2TypeStr</code> is preferred over <code>UUID2Type</code>.<br>
      <br>
      <i>Reasoning:</i> Because casual reading of the type name <code>UUID2Type</code> 
      could be easily misunderstood for a <code>"clazz"</code> of <br>
      <code>Class&lt;UUID2&lt;?&gt;&gt;</code> in plain reading of the name in code. Without the 
      <code>Str</code> at the end, you would need to take an extra step<br>
      to look up the actual type.
    </td>
  </tr>
</table>

