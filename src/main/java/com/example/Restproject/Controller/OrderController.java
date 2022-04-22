package com.example.Restproject.Controller;

import com.example.Restproject.Exception.OrderNotFoundException;
import com.example.Restproject.Model.Assembler.OrderModelAssembler;
import com.example.Restproject.Model.CustomerOrder;
import com.example.Restproject.Repositories.CustomerOrderRepository;
import com.example.Restproject.Status;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.mediatype.problem.Problem;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
public class OrderController {

    @Autowired
    private CustomerOrderRepository orderRepository;

    @Autowired
    private OrderModelAssembler orderModelAssembler;

    /*OrderController(CustomerOrderRepository orderRepository, OrderModelAssembler assembler) {
        this.orderRepository = orderRepository;
        this.orderModelAssembler = assembler;
    }*/

    @GetMapping("/orders")
    public CollectionModel<EntityModel<CustomerOrder>> findAll() {

        List<EntityModel<CustomerOrder>> orders = orderRepository.findAll().stream()
                .map(orderModelAssembler::toModel)
                .collect(Collectors.toList());

        return CollectionModel.of(orders,
                linkTo(methodOn(OrderController.class).findAll()).withSelfRel());
    }

    @GetMapping("/orders/{id}")
    public EntityModel<CustomerOrder> findOne(@PathVariable Long id){
        CustomerOrder order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
        return orderModelAssembler.toModel(order);
    }

    @PostMapping("/orders")
    ResponseEntity<EntityModel<CustomerOrder>> newOrder(@RequestBody CustomerOrder order) {
        order.setStatus(Status.IN_PROGRESS);
        CustomerOrder newOrder = orderRepository.save(order);

        return  ResponseEntity.created(linkTo(methodOn(OrderController.class).findOne(newOrder.getId())).toUri())
                .body(orderModelAssembler.toModel(newOrder));
    }

    @DeleteMapping("/orders/{id}/cancel")
    public ResponseEntity<?> cancel(@PathVariable Long id) {

        CustomerOrder order = orderRepository.findById(id) //
                .orElseThrow(() -> new OrderNotFoundException(id));

        if (order.getStatus() == Status.IN_PROGRESS) {
            order.setStatus(Status.CANCELLED);
            return ResponseEntity.ok(orderModelAssembler.toModel(orderRepository.save(order)));
        }

        return ResponseEntity //
                .status(HttpStatus.METHOD_NOT_ALLOWED) //
                .header(HttpHeaders.CONTENT_TYPE, MediaTypes.HTTP_PROBLEM_DETAILS_JSON_VALUE) //
                .body(Problem.create() //
                        .withTitle("Method not allowed") //
                        .withDetail("You can't cancel an order that is in the " + order.getStatus() + " status"));
    }

    @PutMapping("/orders/{id}/complete")
    public ResponseEntity<?> complete(@PathVariable Long id) {

        CustomerOrder order = orderRepository.findById(id) //
                .orElseThrow(() -> new OrderNotFoundException(id));

        if (order.getStatus() == Status.IN_PROGRESS) {
            order.setStatus(Status.COMPLETED);
            return ResponseEntity.ok(orderModelAssembler.toModel(orderRepository.save(order)));
        }

        return ResponseEntity //
                .status(HttpStatus.METHOD_NOT_ALLOWED) //
                .header(HttpHeaders.CONTENT_TYPE, MediaTypes.HTTP_PROBLEM_DETAILS_JSON_VALUE) //
                .body(Problem.create() //
                        .withTitle("Method not allowed") //
                        .withDetail("You can't complete an order that is in the " + order.getStatus() + " status"));
    }
}
