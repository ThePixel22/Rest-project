package com.example.Restproject.Model.Assembler;

import com.example.Restproject.Controller.OrderController;
import com.example.Restproject.Model.CustomerOrder;
import com.example.Restproject.Model.Employee;
import com.example.Restproject.Status;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import javax.swing.text.html.parser.Entity;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class OrderModelAssembler implements RepresentationModelAssembler<CustomerOrder, EntityModel<CustomerOrder>> {
    @Override
    public EntityModel<CustomerOrder> toModel(CustomerOrder order) {
        EntityModel<CustomerOrder> orderModel = EntityModel.of(order,
                linkTo(methodOn(OrderController.class).findOne(order.getId())).withSelfRel(),
                linkTo(methodOn(OrderController.class).findAll()).withRel("orders"));

        if (order.getStatus() == Status.IN_PROGRESS) {
            orderModel.add(linkTo(methodOn(OrderController.class).cancel(order.getId())).withRel("cancel"));
            orderModel.add(linkTo(methodOn(OrderController.class).complete(order.getId())).withRel("complete"));
        }

        return orderModel;
    }
}
