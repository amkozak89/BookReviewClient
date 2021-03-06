# Book Listing Exercise #

This is the client side project for the Book Listing Exercise. The server side is accessible [here](https://github.com/amkozak89/BookReviewServer)

## Considerations
- The primary goal here was to keep things as simple as possible for development's sake. It may be worth breaking down the `Main` method some more for improved readability and separation of concerns, but given the imperative nature of what's going on it might just lead to things leaning towards spaghetti code. In an enterprise application, this would become much more necessary as features expand.
- This is just executed in line via a single CLI command with arguments. It does not actively wait and consume user input.
- In an enterprise application it would likely be valuable for this project to consume a `models` project as provided by the server to prevent duplication of POJOs between the server/client source. That seemed to complicated for the simple project.
