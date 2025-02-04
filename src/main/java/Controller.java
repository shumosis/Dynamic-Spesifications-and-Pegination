
@RestController
@RequestMapping("/v1/api/staffs")
public class Controller {

    @PostMapping("/pagination-filter")
    @PreAuthorize("hasPermission('staffs', 'READ')")
    @Operation(summary = "Get All Staff Records", description = "Retrieve all Staff records for a specific school. Requires admin rights for the school.")
    @ApiResponse(responseCode = "200", description = "List of Staff records", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StaffDto.class)))
    public ResponseEntity<Page<StaffDto>> findAllWithPaginationAndFilter(
            @RequestBody PageRequestParams pageRequestParams) {
        Page<StaffDto> staffDtos = staffService.findAllByCriteria( pageRequestParams);
        return ResponseEntity.ok(staffDtos);
    }
}
